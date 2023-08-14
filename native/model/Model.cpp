/*
 * Copyright (c) 2020 Bytedance Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Model_CPP_
#define Model_CPP_

#include "Model.h"
#include "StateFactory.h"
#include "../utils.hpp"
#include <ctime>
#include <iostream>

namespace fastbotx {


    std::shared_ptr<Model> Model::create() {
        return ModelPtr(new Model());
    }

    Model::Model() {
#ifndef FASTBOT_VERSION
#define FASTBOT_VERSION "local build"
#endif
        BLOG("---- native version " FASTBOT_VERSION " native version ----\n");
        this->_graph = std::make_shared<Graph>();
        this->_preference = Preference::inst();
        this->_netActionParam.netActionTaskid = 0;
    }


/// The general entrance of getting next step according to RL model, and return the next operation step in json format
/// \param descContent XML of the current page, in string format
/// \param activity activity name
/// \param deviceID The default value is "", you could provide your intended ID
/// \return the next operation step in json format
    std::string Model::getOperate(const std::string &descContent, const std::string &activity,
                                  const std::string &deviceID) //the entry for getting a new operation
    {
        const std::string &descContentCopy = descContent;
        ElementPtr elem = Element::createFromXml(
                descContentCopy); // get the xml object with tinyxml2
        if (nullptr == elem)
            return "";
        return this->getOperate(elem, activity, deviceID);
    }


#define DefaultDeviceID "0000001"

    AbstractAgentPtr Model::addAgent(const std::string &deviceIDString, AlgorithmType agentType,
                                     DeviceType deviceType) {
        auto agent = AgentFactory::create(agentType, shared_from_this(), deviceType);
        const std::string &deviceID = deviceIDString.empty() ? DefaultDeviceID
                                                             : deviceIDString; // deviceID is device id
        this->_deviceIDAgentMap.emplace(deviceID,
                                        agent); // add the pair of device and agent to the _deviceIDAgentMap
        this->_graph->addListener(
                agent); // add the agent to the graph and listen to updates of this graph.
        return agent;
    }

    AbstractAgentPtr Model::getAgent(const std::string &deviceID) const {
        const std::string &d = deviceID.empty() ? DefaultDeviceID : deviceID;
        auto iter = this->_deviceIDAgentMap.find(d);
        if (iter != this->_deviceIDAgentMap.end())
            return (*iter).second;
        return nullptr;
    }


    std::string Model::getOperate(const ElementPtr &element, const std::string &activity,
                                  const std::string &deviceID) {
        OperatePtr operate = getOperateOpt(element, activity, deviceID);
        std::string operateString = operate->toString(); // wrap the operation as a json object and get its string
        return operateString;
    }


    OperatePtr Model::getOperateOpt(const ElementPtr &element, const std::string &activity,
                                    const std::string &deviceID) {
        // the whole process begins.
        double methodStartTimestamp = currentStamp(); //the time stamp of this current time
        ActionPtr customActionPtr = nullptr;
        if (this->_preference) //load the preferred action in preference file specified by user in sdcard
        {
            BLOG("try get custom action from preference");
            customActionPtr = this->_preference->resolvePageAndGetSpecifiedAction(activity,
                                                                                  element);
        }
        // get activity
        stringPtrSet activityStringPtrSet = this->_graph->getVisitedActivities();
        stringPtr activityPtr = std::make_shared<std::string>(activity);
        auto founded = activityStringPtrSet.find(
                activityPtr); //Searches the container for an element equivalent to val and returns an iterator to it if found, otherwise it returns an iterator to set::end.
        stringPtr activityStringPtr = nullptr;
        if (founded == activityStringPtrSet.end())
            activityStringPtr = activityPtr; //this is a new activity.
        else
            activityStringPtr = *founded; // use the cached activity.
        //  get agent
        if (this->_deviceIDAgentMap.empty())  // create a default agent
        {
            BLOG("%s", "use reuseAgent as the default agent");
            this->addAgent(DefaultDeviceID, AlgorithmType::Reuse);
        }
        auto agentIterator = this->_deviceIDAgentMap.find(deviceID);
        AbstractAgentPtr agent = nullptr;
        if (agentIterator == this->_deviceIDAgentMap.end())
            agent = this->_deviceIDAgentMap[DefaultDeviceID]; // get the agent from the default device
        else
            agent = (*agentIterator).second; // get the found agent

        // get state
        StatePtr state = nullptr;
        if (nullptr != element) // make sure the XML is not null
        {
            //according to the type of the used agent, create the state of this page
            //include all the possible actions according to the widgets inside.
            state = StateFactory::createState(agent->getAlgorithmType(), activityStringPtr,
                                              element);
            // add state
            // add this state, and the agent will treat this state as the new state(_newState)
            state = this->_graph->addState(state);
            state->visit(this->_graph->getTimestamp());
        }

        // new state is prepared, record the current time
        double stateGeneratedTimestamp = currentStamp();
        ActionPtr action = customActionPtr; // load the action specified by user

        BDLOGE("%s", state->toString().c_str());

        double startGeneratingActionTimestamp = currentStamp();
        double endGeneratingActionTimestamp = currentStamp();
        bool shouldSkipActionsFromModel = this->_preference->skipAllActionsFromModel();
        if (shouldSkipActionsFromModel) // seems that user could specify if the model should be used?
        {
            LOGI("listen mode skip get action from model");
        }

        // if there is no action specified by user, ask the agent for a new action.
        if (nullptr == customActionPtr && !shouldSkipActionsFromModel) {
            if (-1 != BLOCK_STATE_TIME_RESTART &&
                -1 != Preference::inst()->getForceMaxBlockStateTimes() &&
                agent->getCurrentStateBlockTimes() > BLOCK_STATE_TIME_RESTART) {
                action = Action::RESTART;
                BLOG("Ran into a block state %s", state ? state->getId().c_str() : "");
            } else {
                // this is also an entry for modifying RL model
                action = std::dynamic_pointer_cast<Action>(agent->resolveNewAction());
                // update the strategy based on the new action
                agent->updateStrategy();
                if (nullptr == action) {
                    BDLOGE("get null action!!!!");
                    // handle null action by returning the nop operation to the upper caller.
                    return DeviceOperateWrapper::OperateNop;
                }
            }
            endGeneratingActionTimestamp = currentStamp();
            if (action->isModelAct() && state) {
                action->visit(this->_graph->getTimestamp());
                agent->moveForward(state); // update _currentState/Action with _newState/Action
            }
        }


        //new action generated, RL model is updated, record the current time.
        OperatePtr opt = DeviceOperateWrapper::OperateNop;
        if (action != nullptr) {
            BLOG("selected action %s", action->toString().c_str());
            opt = action->toOperate();
            if (this->_preference) {
                this->_preference->patchOperate(opt);
            }

            if (DROP_DETAIL_AFTER_SATE && state && !state->hasNoDetail())
                state->clearDetails();
        }
        // the whole process end, record the current time.
        double methodEndTimestamp = currentStamp();
        BLOG("build state cost: %.3fs action cost: %.3fs total cost %.3fs",
             stateGeneratedTimestamp - methodStartTimestamp,
             endGeneratingActionTimestamp - startGeneratingActionTimestamp,
             methodEndTimestamp - methodStartTimestamp);
        return opt;
    }

    Model::~Model() {
        this->_deviceIDAgentMap.clear();
    }

}
#endif //Model_CPP_

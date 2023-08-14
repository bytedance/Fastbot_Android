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
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang, Zhengwei Lv
 */
#ifndef fastbotx_ModelReusableAgent_CPP_
#define fastbotx_ModelReusableAgent_CPP_

#include "ModelReusableAgent.h"
#include "Model.h"
#include <cmath>
#include "ActivityNameAction.h"
#include "ReuseModel_generated.h"
#include <iostream>
#include <fstream>
#include <limits>
#include <mutex>
#include <utility>


namespace fastbotx {

    ModelReusableAgent::ModelReusableAgent(const ModelPtr &model)
            : AbstractAgent(model), _alpha(SarsaRLDefaultAlpha), _epsilon(SarsaRLDefaultEpsilon),
              _modelSavePath(DefaultModelSavePath), _defaultModelSavePath(DefaultModelSavePath) {
        this->_algorithmType = AlgorithmType::Reuse;
    }

    ModelReusableAgent::~ModelReusableAgent() {
        BLOG("save model in destruct");
        this->saveReuseModel(this->_modelSavePath);
        this->_reuseModel.clear();
    }

    void ModelReusableAgent::computeAlphaValue() {
        if (nullptr != this->_newState) {
            /// actually, the following part of computing alpha could be extracted and treated as a single method
            const GraphPtr &graphRef = this->_model.lock()->getGraph(); // won't this cause null pointer issue? since the lock of weak_ptr could be null?
            long totalVisitCount = graphRef->getTotalDistri(); // get the total count of visited states
            double movingAlpha = 0.5;
            if (totalVisitCount >
                20000)  // if the total count of visited states is too much, reduce the alpha.
            {
                movingAlpha -= 0.1;
            }
            if (totalVisitCount > 50000) {
                movingAlpha -= 0.1;
            }
            if (totalVisitCount > 100000) {
                movingAlpha -= 0.1;
            }
            if (totalVisitCount > 250000) {
                movingAlpha -= 0.1;
            }
            // after reducing, the possible minimal alpha is 0.1
            // but the actually possible minimal alpha is 0.2, the same as SarsaRLDefaultAlpha
            this->_alpha = std::max(SarsaRLDefaultAlpha, movingAlpha);
        }
    }

#define SarsaNStep 5

    /// Based on the lastSelectedAction (newly selected action), compute its reward value
    /// \return the reward value
    double ModelReusableAgent::computeRewardOfLatestAction() {
        double rewardValue = 0.0;
        if (nullptr != this->_newState) {
            this->computeAlphaValue();
            const GraphPtr &graphRef = this->_model.lock()->getGraph();
            auto visitedActivities = graphRef->getVisitedActivities(); // get the set of visited activities
            // get the last, or previous, action in the vector containing previous actions.
            ActivityStateActionPtr lastSelectedAction = std::dynamic_pointer_cast<ActivityStateAction>(
                    this->_previousActions.back());
            if (nullptr != lastSelectedAction) {
                // Get the expectation of this action for accessing unvisited new activity.
                rewardValue = this->probabilityOfVisitingNewActivities(lastSelectedAction,
                                                                       visitedActivities);
                // If this is an action not in reuse model, this action is new and should definitely be used
                if (std::abs(rewardValue - 0.0) < 0.0001)
                    rewardValue = 1.0; // Set the expectation of this action to 1
                rewardValue = (rewardValue / sqrt(lastSelectedAction->getVisitedCount() + 1.0));
            }
            rewardValue = rewardValue + (this->getStateActionExpectationValue(this->_newState,
                                                                              visitedActivities) /
                                         sqrt(this->_newState->getVisitedCount() + 1.0));
            BLOG("total visited " ACTIVITY_VC_STR " count is %zu", visitedActivities.size());
        }
        BDLOG("reuse-cov-opti action reward=%f", rewardValue);
        this->_rewardCache.emplace_back(rewardValue);
        // Make sure the length of reward cache is not over SarsaNStep
        if (this->_rewardCache.size() > SarsaNStep) {
            this->_rewardCache.erase(this->_rewardCache.begin());
        }
        return rewardValue;// + this->_newState->getTheta();
    }

    /// Based on the reuse model, compute the probability of this current action visiting a unvisited activity,
    /// which not in visitedActivities set. This value is the percentage of count of
    /// activities that this state has not reached compared with the visitedActivities set.
    /// \param action The chosen action in this state.
    /// \param visitedActivities A string set, containing already visited activities.
    /// \return percentage of count of activities that this state has not reached compared with the visitedActivities set.
    double
    ModelReusableAgent::probabilityOfVisitingNewActivities(const ActivityStateActionPtr &action,
                                                           const stringPtrSet &visitedActivities) const {
        double value = .0;
        int total = 0;
        int unvisited = 0;
        // find this action in this model according to its int hash
        // according to the given action, get the activities that this action could reach in reuse model.
        auto actionMapIterator = this->_reuseModel.find(action->hash());
        if (actionMapIterator != this->_reuseModel.end()) {
            // Iterate the map containing entry of activity name and visited count
            // to ascertain the unvisited activity count according to the pre-saved reuse model
            for (const auto &activityCountMapIterator: (*actionMapIterator).second) {
                total += activityCountMapIterator.second;
                stringPtr activity = activityCountMapIterator.first;
                if (visitedActivities.find(activity) == visitedActivities.end()) {
                    unvisited += activityCountMapIterator.second;
                }
            }
            if (total > 0 && unvisited > 0) {
                value = static_cast<double>(unvisited) / total;
            }
        }
        return value;
    }

    /// Return the expectation of reaching an unvisited activity after executing one of the action
    /// from this state. It estimate the expectation from the perspective of the whole state.
    /// @param state the newly reached state
    /// @param visitedActivities the visited activity set AFTER reaching this state(the activity of this
    ///         state is included)
    /// @return the expectation of this state reaching an unvisited activity after executing one of the action
    double ModelReusableAgent::getStateActionExpectationValue(const StatePtr &state,
                                                              const stringPtrSet &visitedActivities) const {
        double value = 0.0;
        for (const auto &action: state->getActions()) {
            uintptr_t actionHash = action->hash();
            // if this action is new, increment the value by 1, else by 0.5
            // If this action has not been visited yet.
            if (this->_reuseModel.find(actionHash) == this->_reuseModel.end()) {
                value += 1.0;
            }
                // If this action is been performed in current testing.
            else if (action->getVisitedCount() >= 1) {
                value += 0.5;
            }

            // regardless of the back action
            // Expectation of reaching an unvisited activity.
            if (action->getTarget() != nullptr) {
                value += probabilityOfVisitingNewActivities(action, visitedActivities);
            }
        }
        return value;
    }

    double ModelReusableAgent::getQValue(const ActionPtr &action) {
        return action->getQValue();
    }

    void ModelReusableAgent::setQValue(const ActionPtr &action, double qValue) {
        action->setQValue(qValue);
    }

/// If the new action is generated,
    void ModelReusableAgent::updateStrategy() {
        if (nullptr == this->_newAction) // need to call resolveNewAction to update _newAction
            return;
        // _previousActions is a vector storing certain amount of actions, of which length equals to SarsaNStep.
        if (!this->_previousActions.empty()) {
            this->computeRewardOfLatestAction();
            this->updateReuseModel();
            double value = getQValue(_newAction);
            for (int i = static_cast<int>(this->_previousActions.size()) - 1; i >= 0; i--) {
                double currentQValue = getQValue(_previousActions[i]);
                double currentRewardValue = this->_rewardCache[i];
                // accumulated reward from the newest actions
                value = currentRewardValue + SarsaRLDefaultGamma * value;
                // Should not update the q value during step (action edge) between i+1 to i+n-1
                // The following statement is slightly different from the original sarsa RL paper.
                // Considering to move the next statement outside of this block.
                // Since only the oldest action should be updated.
                if (i == 0)
                    setQValue(this->_previousActions[i],
                              currentQValue + this->_alpha * (value - currentQValue));
            }
        } else {
            BDLOG("%s", "get action value failed!");
        }
        // add the new action to the back of the cache.
        this->_previousActions.emplace_back(this->_newAction);
        if (this->_previousActions.size() > SarsaNStep) {
            // if the cached length is over SarsaNStep, erase the first action from cache.
            this->_previousActions.erase(this->_previousActions.begin());
        }
    }

    void ModelReusableAgent::updateReuseModel() {
        if (this->_previousActions.empty())
            return;
        ActionPtr lastAction = this->_previousActions.back();
        ActivityNameActionPtr modelAction = std::dynamic_pointer_cast<ActivityNameAction>(
                lastAction);
        if (nullptr == modelAction || nullptr == this->_newState)
            return;
        auto hash = (uint64_t) modelAction->hash();
        stringPtr activity = this->_newState->getActivityString(); // mark: use the _newstate as last selected action's target
        if (activity == nullptr)
            return;
        {
            std::lock_guard<std::mutex> reuseGuard(this->_reuseModelLock);
            auto iter = this->_reuseModel.find(hash);
            if (iter == this->_reuseModel.end()) {
                BDLOG("can not find action %s in reuse map", modelAction->getId().c_str());
                ReuseEntryM entryMap;
                entryMap.emplace(std::make_pair(activity, 1));
                this->_reuseModel[hash] = entryMap;
            } else {
                ((*iter).second)[activity] += 1;
            }
            auto qValueReuseEntryIter = this->_reuseQValue.find(hash);
            this->_reuseQValue[hash] = modelAction->getQValue();
        }
    }

    ActivityStateActionPtr ModelReusableAgent::selectNewActionEpsilonGreedyRandomly() const {
        if (this->eGreedy()) {
            BDLOG("%s", "Try to select the max value action");
            return this->_newState->greedyPickMaxQValue(enableValidValuePriorityFilter);
        }
        BDLOG("%s", "Try to randomly select a value action.");
        return this->_newState->randomPickAction(enableValidValuePriorityFilter);
    }


    bool ModelReusableAgent::eGreedy() const {
        srand((uint32_t) (int) time(nullptr)); // @TODO the random range
        auto r = static_cast<double>(static_cast<double>(rand() % 100 ) / 100.0L);
        if (r < this->_epsilon)
            return false;
        return true;
    }


    ActionPtr ModelReusableAgent::selectNewAction() {
        ActionPtr action = nullptr;
        action = this->selectUnperformedActionNotInReuseModel();
        if (nullptr != action) {
            BLOG("%s", "select action not in reuse model");
            return action;
        }

        action = this->selectUnperformedActionInReuseModel();
        if (nullptr != action) {
            BLOG("%s", "select action in reuse model");
            return action;
        }

        action = this->_newState->randomPickUnvisitedAction();
        if (nullptr != action) {
            BLOG("%s", "select action in unvisited action");
            return action;
        }

        // if all the actions are explored, use those two methods to generate new action based on q value.
        // there are two methods to choose from.
        // based on q value and a uniform distribution, select an action with the highest value.
        action = this->selectActionByQValue();
        if (nullptr != action) {
            BLOG("%s", "select action by qvalue");
            return action;
        }

        // use the traditional epsilon greedy strategy to choose the next action.
        action = this->selectNewActionEpsilonGreedyRandomly();
        if (nullptr != action) {
            BLOG("%s", "select action by EpsilonGreedyRandom");
            return action;
        }
        BLOGE("null action happened , handle null action");
        return handleNullAction();
    }

    /// Randomly choose an action that is not been visited before, which belongs to the following type:
    ///        BACK,
    ///        FEED,
    ///        CLICK,
    ///        LONG_CLICK,
    ///        SCROLL_TOP_DOWN,
    ///        SCROLL_BOTTOM_UP,
    ///        SCROLL_LEFT_RIGHT,
    ///        SCROLL_RIGHT_LEFT,
    ///        SCROLL_BOTTOM_UP_N
    /// \return An action in this new state but not been performed before nor been recorded by Reuse Model
    ActionPtr ModelReusableAgent::selectUnperformedActionNotInReuseModel() const {
        ActionPtr retAct = nullptr;
        std::vector<ActionPtr> actionsNotInModel;
        for (const auto &action: this->_newState->getActions()) {
            bool matched = action->isModelAct() // should be one of aforementioned actions.
                           && (this->_reuseModel.find(action->hash()) ==
                               this->_reuseModel.end()) // this action should not be in reuse model
                           && action->getVisitedCount() <=
                              0; // find the action that not been explored before
            if (matched) {
                actionsNotInModel.emplace_back(action);
            }
        }
        // random by priority
        int totalWeight = 0;
        for (const auto &action: actionsNotInModel) {
            totalWeight += action->getPriority();
        }
        if (totalWeight <= 0) {
            BDLOGE("%s", " total weights is 0");
            return nullptr;
        }
        int randI = randomInt(0, totalWeight);
        for (auto action: actionsNotInModel) {
            if (randI < action->getPriority()) {
                return action;
            }
            randI -= action->getPriority();
        }
        BDLOGE("%s", " rand a null action");
        return nullptr;
    }

    ActionPtr ModelReusableAgent::selectUnperformedActionInReuseModel() const {
        float maxValue = -MAXFLOAT;
        ActionPtr nextAction = nullptr;
        // use humble gumbel(http://amid.fish/humble-gumbel) to affect the sampling of actions from reuseModel
        for (const auto &action: this->_newState->targetActions())  // except BACK/FEED/EVENT_SHELL actions. Only actions from  ActionType::CLICK to ActionType::SCROLL_BOTTOM_UP_N are allowed
        {
            uintptr_t actionHash = action->hash();
            if (this->_reuseModel.find(actionHash) !=
                this->_reuseModel.end()) // found this action in reuse model
            {
                if (action->getVisitedCount() >
                    0) // In this state, this action has just been performed in this round.
                {
                    BDLOG("%s", "action has been visited");
                    continue;
                }
                auto modelPointer = this->_model.lock();
                if (modelPointer) {
                    const GraphPtr &graphRef = modelPointer->getGraph();
                    auto visitedActivities = graphRef->getVisitedActivities();
                    auto qualityValue = static_cast<float>(this->probabilityOfVisitingNewActivities(
                            action,
                            visitedActivities));
                    if (qualityValue >
                        1e-4) // quality value of candidate action should be larger than 0
                    {
                        // following code is for generating a random value to slight affect the quality value
                        qualityValue = 10.0f * qualityValue;
                        auto uniform = static_cast<float>(static_cast<float>(randomInt(0, 10)) /
                                                          10.0f);
                        // random value from uniform distribution should not be 0, or log function will return INF
                        if (uniform < std::numeric_limits<float>::min())
                            uniform = std::numeric_limits<float>::min();
                        // add this random factor to quality value
                        qualityValue -= log(-log(uniform));

                        // choose the action with the maximum quality value
                        if (qualityValue > maxValue) {
                            maxValue = qualityValue;
                            nextAction = action;
                        }
                    }
                }
            }
        }
        return nextAction;
    }

#define entropyAlpha  0.1

    /// Select an action with the largest quality value based on
    /// its quality value and the uniform distribution
    /// \return the selected action with the highest quality value
    ActionPtr ModelReusableAgent::selectActionByQValue() {
        ActionPtr returnAction = nullptr;
        float maxQ = -MAXFLOAT;
        const GraphPtr &graphRef = this->_model.lock()->getGraph();
        auto visitedActivities = graphRef->getVisitedActivities();
        for (auto action: this->_newState->getActions()) {
            double qv = 0.0;
            uintptr_t actionHash = action->hash();
            // it won't happen, since if there is am unvisited action in state, it will be
            // visited before this method is called.
            if (action->getVisitedCount() <= 0) {
                auto iterator = this->_reuseModel.find(actionHash);
                if (iterator != this->_reuseModel.end()) {
                    qv += this->probabilityOfVisitingNewActivities(action, visitedActivities);
                } else {
                    BDLOG("qvalue pick return a action: %s", action->toString().c_str());
                    return action;
                }
            }
            qv += getQValue(action);
            qv /= entropyAlpha;
            float uniform = static_cast<float>(randomInt(0, 10)) /
                            10.0f; // with this uniform distribution, add a little disturbance to the qv value

            // use the uniform distribution and humble gumbel to add some randomness to the qv value
            if (uniform < std::numeric_limits<float>::min())
                uniform = std::numeric_limits<float>::min();
            qv -= log(-log(uniform));
            // choose the action with the highest qv value
            if (qv > maxQ) {
                maxQ = static_cast<float >(qv);
                returnAction = action;
            }
        }
        return returnAction; // return the action with the largest qv value
    }

    void ModelReusableAgent::adjustActions() {
        AbstractAgent::adjustActions();
    }

    void ModelReusableAgent::threadModelStorage(const std::weak_ptr<ModelReusableAgent> &agent) {
        int saveInterval = 1000 * 60 * 10; // save model per 10 min
        while (!agent.expired()) {
            agent.lock()->saveReuseModel(agent.lock()->_modelSavePath);
            std::this_thread::sleep_for(std::chrono::milliseconds(saveInterval));
        }
    }

#ifdef __ANDROID__
#define STORAGE_PREFIX "/sdcard/fastbot_"
#else
#define STORAGE_PREFIX ""
#endif

    /// According to the given package name, deserialize
    /// the serialized model file with the ReuseModel.fbs
    /// by FlatBuffers
    /// \param packageName The package name of the tested application
    void ModelReusableAgent::loadReuseModel(const std::string &packageName) {
        std::string modelFilePath = STORAGE_PREFIX + packageName + ".fbm";

        this->_modelSavePath = modelFilePath;
        if (!this->_modelSavePath.empty()) {
            this->_defaultModelSavePath = STORAGE_PREFIX + packageName + ".tmp.fbm";
        }
        BLOG("begin load model: %s", this->_modelSavePath.c_str());

        std::ifstream modelFile(modelFilePath, std::ios::binary | std::ios::in);
        if (modelFile.fail()) {
            BLOG("read model file %s failed, check if file exists!", modelFilePath.c_str());
            return;
        }

        // load  model file to struct
        std::filebuf *fileBuffer = modelFile.rdbuf();
        // returns the new position of the altered control stream, which is the end of the file
        std::size_t filesize = fileBuffer->pubseekoff(0, modelFile.end, modelFile.in);
        // reset the position of the controlled stream
        fileBuffer->pubseekpos(0, modelFile.in);
        char *modelFileData = new char[filesize];
        //Reads count characters from the input sequence and stores them into a character array.
        fileBuffer->sgetn(modelFileData, static_cast<int>(filesize));
        auto reuseFBModel = GetReuseModel(modelFileData);

        // to std::map
        {
            std::lock_guard<std::mutex> reuseGuard(this->_reuseModelLock);
            this->_reuseModel.clear();
            this->_reuseQValue.clear();
        }
        auto reusedModelDataPtr = reuseFBModel->model();
        if (!reusedModelDataPtr) {
            BLOG("%s", "model data is null");
            return;
        }
        for (int entryIndex = 0; entryIndex < reusedModelDataPtr->size(); entryIndex++) {
            auto reuseEntryInReuseModel = reusedModelDataPtr->Get(entryIndex);
            uint64_t actionHash = reuseEntryInReuseModel->action();
            auto activityEntry = reuseEntryInReuseModel->targets();
            ReuseEntryM entryPtr;
            for (int targetIndex = 0; targetIndex < activityEntry->size(); targetIndex++) {
                auto targetEntry = activityEntry->Get(targetIndex);
                BDLOG("load model hash: %llu %s %d", actionHash,
                      targetEntry->activity()->str().c_str(), (int) targetEntry->times());
                entryPtr.insert(std::make_pair(
                        std::make_shared<std::string>(targetEntry->activity()->str()),
                        (int) targetEntry->times()));
            }
            if (!entryPtr.empty()) {
                std::lock_guard<std::mutex> reuseGuard(this->_reuseModelLock);
//            this->_reuseQValue.insert(std::make_pair(actionHash, reuseEntryInReuseModel->quality()));
                this->_reuseModel.insert(std::make_pair(actionHash, entryPtr));
            }
        }
        BLOG("loaded model contains actions: %zu", this->_reuseModel.size());
        delete[] modelFileData;
    }

    std::string ModelReusableAgent::DefaultModelSavePath = "/sdcard/fastbot.model.fbm";

    /// With the FlatBuffer library, serialize the ReuseModel according to ReuseModel.fbs,
    /// and save the data to modelFilePath.
    /// \param modelFilepath the path to save this serialized model.
    void ModelReusableAgent::saveReuseModel(const std::string &modelFilepath) {
        flatbuffers::FlatBufferBuilder builder;
        std::vector<flatbuffers::Offset<fastbotx::ReuseEntry>> actionActivityVector;
        // loaded, but not visited
        {
            std::lock_guard<std::mutex> reuseGuard(this->_reuseModelLock);
            for (const auto &actionIterator: this->_reuseModel) {
                uint64_t actionHash = actionIterator.first;
                ReuseEntryM activityCountEntryMap = actionIterator.second;
                std::vector<flatbuffers::Offset<fastbotx::ActivityTimes>> activityCountEntryVector; // flat buffer needs vector rather than map
                for (const auto &activityCountEntry: activityCountEntryMap) {
                    auto sentryActT = CreateActivityTimes(builder, builder.CreateString(
                            *(activityCountEntry.first)), activityCountEntry.second);
                    activityCountEntryVector.push_back(sentryActT);
                }
                auto savedActivityCountEntries = CreateReuseEntry(builder, actionHash,
                                                                  builder.CreateVector(
                                                                          activityCountEntryVector.data(),
                                                                          activityCountEntryVector.size()));
                actionActivityVector.push_back(savedActivityCountEntries);
            }
        }
        auto savedActionActivityEntries = CreateReuseModel(builder, builder.CreateVector(
                actionActivityVector.data(), actionActivityVector.size()));
        builder.Finish(savedActionActivityEntries);

        //save to local file
        std::string outputFilePath = modelFilepath;
        if (outputFilePath.empty()) // if the passed argument modelFilepath is "", use the tmpSavePath
            outputFilePath = this->_defaultModelSavePath;
        BLOG("save model to path: %s", outputFilePath.c_str());
        std::ofstream outputFile(outputFilePath);
        outputFile.write((char *) builder.GetBufferPointer(), static_cast<int>(builder.GetSize()));
        outputFile.close();
    }

}

#endif

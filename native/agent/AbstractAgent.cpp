/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */

#ifndef AbstractAgent_CPP_
#define AbstractAgent_CPP_

#include "AbstractAgent.h"

#include <utility>
#include "../model/Model.h"

namespace fastbotx {

    AbstractAgent::AbstractAgent()
            : _validateFilter(validDatePriorityFilter), _graphStableCounter(0),
              _stateStableCounter(0), _activityStableCounter(0), _disableFuzz(false),
              _requestRestart(false), _currentStateBlockTimes(0),
              _algorithmType(AlgorithmType::Random) {

    }


    AbstractAgent::AbstractAgent(const ModelPtr &model)
            : AbstractAgent() {
        this->_model = model;
    }

    AbstractAgent::~AbstractAgent() {
        this->_model.reset();
        this->_lastState.reset();
        this->_currentState.reset();
        this->_newState.reset();
        this->_lastAction.reset();
        this->_currentAction.reset();
        this->_newAction.reset();
        this->_validateFilter.reset();
    }


    void AbstractAgent::onAddNode(StatePtr node) {
        _newState = node;

        if(BLOCK_STATE_TIME_RESTART != -1)
        {
            if (equals(_newState, _currentState)) {
                this->_currentStateBlockTimes++;
            } else {
                this->_currentStateBlockTimes = 0;
            }
        }
    }


    void AbstractAgent::moveForward(StatePtr nextState) {
        //update state
        _lastState = _currentState;
        _currentState = _newState;
        _newState = std::move(nextState);
        //update action
        _lastAction = _currentAction;
        _currentAction = _newAction;
        _newAction = nullptr;
    }

    void AbstractAgent::adjustActions() {
        double totalPriority = 0; // accumulate all the priorities from actions of this state
        for (const ActivityStateActionPtr &action: _newState->getActions()) {
            // click has priority of 4, other priority is 2, why?
            int basePriority = action->getPriorityByActionType();
            action->setPriority(basePriority);
            if (!action->requireTarget()) {
                if (!action->isVisited()) {
                    int priority = action->getPriority();
                    priority += 5;
                    action->setPriority(priority);
                }
                continue;
            }
            if (!action->isValid()) {
                continue;
            }
            int priority = action->getPriority();
            if (!action->isVisited()) {
                priority += 20; // Select unvisited priority
            }
            if (!this->_newState->isSaturated(action))// if this is a new action
            {
                priority += 5 * action->getPriorityByActionType();
            }

            if (priority <= 0) {
                priority = 0;
            }

            action->setPriority(priority); // set the priority to each action.
            totalPriority += (priority - action->getPriorityByActionType());
        }
        _newState->setPriority((int) totalPriority);
    }

    ActionPtr AbstractAgent::resolveNewAction() {
        // update priority
        this->adjustActions();
        ActionPtr action = this->selectNewAction();
        _newAction = std::dynamic_pointer_cast<ActivityStateAction>(action);
        return action;
    }


    ActivityStateActionPtr AbstractAgent::handleNullAction() const {
        ActivityStateActionPtr action = this->_newState->randomPickAction(this->_validateFilter);
        if (nullptr != action) {
            ActivityStateActionPtr resolved = this->_newState->resolveAt(action,
                                                                         this->_model.lock()->getGraph()->getTimestamp());
            if (nullptr != resolved) {
                return resolved;
            }
        }
        BDLOGE("handle null action error!!!!!");
        return nullptr;
    }
}

#endif //AbstractAgent_CPP_

/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */

#ifndef AbstractAgent_H_
#define AbstractAgent_H_


#include "utils.hpp"
#include "Base.h"
#include "Action.h"
#include "State.h"
#include "ActionFilter.h"
#include "Graph.h"

namespace fastbotx {

    class Model; // forward declaration
    typedef std::shared_ptr<Model> ModelPtr;

    class AbstractAgent : public GraphListener {
    public:

        virtual int getCurrentStateBlockTimes() const { return this->_currentStateBlockTimes; }

        // resolve an action and update graph;
        virtual ActionPtr resolveNewAction();


        virtual void updateStrategy() = 0;

        virtual void moveForward(StatePtr nextState);

        // override
        void onAddNode(StatePtr node) override;

        explicit AbstractAgent(const ModelPtr &model);

        virtual ~AbstractAgent();

        virtual AlgorithmType getAlgorithmType() { return this->_algorithmType; }

    protected:

        virtual ActivityStateActionPtr handleNullAction() const;

        virtual ActionPtr selectNewAction() = 0;

        // ！implements in  subclass
        virtual void adjustActions();


        AbstractAgent();


        std::weak_ptr<Model> _model;
        StatePtr _lastState;
        StatePtr _currentState; // it actually represents the last state, but will be updated by _newState
        StatePtr _newState;
        ActivityStateActionPtr _lastAction; // the executed last action for reaching the last state
        ActivityStateActionPtr _currentAction; // the executed new action for reaching the new state
        ActivityStateActionPtr _newAction; // the newly chosen action among candidate actions, for reaching new state.

//    ActionRecordPtrVec  _actionHistory;

        ActionFilterPtr _validateFilter;

        long _graphStableCounter;
        long _stateStableCounter;
        long _activityStableCounter;

        bool _disableFuzz;
        bool _requestRestart;
        bool _appActivityJustStartedFromClean{};
        bool _appActivityJustStarted{};
        bool _currentStateRecovered{};
        int _currentStateBlockTimes;

        AlgorithmType _algorithmType;

    };


    typedef std::shared_ptr<AbstractAgent> AbstractAgentPtr;
    typedef std::vector<AbstractAgentPtr> AbstractAgentPtrVec;
    typedef std::map<std::string, AbstractAgentPtr> AbstractAgentPtrStrMap;
}


#endif //AbstractAgent_H_

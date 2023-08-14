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
 * @authors Jianqiang Guo, Yuhui Su
 */
#ifndef Action_H_
#define Action_H_

#include "Node.h"
#include "Base.h"
#include "Widget.h"
#include "DeviceOperateWrapper.h"

#include <utility>
#include <vector>
#include <set>
#include <string>

namespace fastbotx {


    class State;

/// Base class for all Action classes
    class Action : public Node, public PriorityNode, public HashNode {
    public:
        Action();

        explicit Action(ActionType actionType);

        std::string toString() const override;

        virtual bool getEnabled() const { return true; }

        ActionType getActionType() const { return this->_actionType; }

        void setPriority(int priority);

        int getPriorityByActionType() const;

        bool isBack() const { return this->_actionType == ActionType::BACK; }

        bool isClick() const { return this->_actionType == ActionType::CLICK; }

        bool isNop() const { return this->_actionType == ActionType::NOP; }

        virtual bool isValid() const;

        virtual OperatePtr toOperate() const;

        uintptr_t _hashcode{};

        uintptr_t hash() const override { return _hashcode; }

        bool isModelAct() const;

        bool requireTarget() const;

        bool canStartTestApp() const;

        static std::shared_ptr<Action> NOP;
        static std::shared_ptr<Action> ACTIVATE;
        static std::shared_ptr<Action> RESTART;

        FuncGetID(Action);

        bool operator==(const Action &action);

        virtual ~Action() = default;


        virtual void setQValue(double value) { this->_qValue = static_cast<float>(value); }

        virtual double getQValue() const { return this->_qValue; }

        static int getThrottle() { return _throttle; }

        static const int DefaultValue;

    protected:

        ActionType _actionType;
        static int _throttle;
    private:
        float _qValue;
        PropertyIDPrefix(Action);
    };

    typedef std::shared_ptr<Action> ActionPtr;

    typedef struct NetActionParameter_ {
        int throttle;
        int netActionTaskid;
        std::string algorithmString;
        std::string packageName;
        std::string token;
        std::string deviceid;
    } NetActionParam;

/// Embedding an action with the whole activity state, the target widget and the action to perform.
    class ActivityStateAction : public Action {
    public:
        /// Embedding an action with the whole activity state, the target targetWidget and the type of action to perform.
        /// \param state
        /// \param targetWidget
        /// \param actionType
        ActivityStateAction(const std::shared_ptr<State> &state, WidgetPtr targetWidget,
                            ActionType actionType);

        std::weak_ptr<State> getState() const { return this->_state; }

        std::shared_ptr<Widget> getTarget() const { return this->_target; }

        bool getEnabled() const override;

        bool isValid() const override;

        // set target widget without updating hash code
        void setTarget(WidgetPtr widget) { this->_target = std::move(widget); }

        OperatePtr toOperate() const override;


        // from ResolveNode
        bool isEmpty() const;


        uintptr_t hash() const override;

        bool operator==(const ActivityStateAction &action) const;

        bool operator<(const ActivityStateAction &action) const;

        std::string toString() const override;

        std::weak_ptr<State> _state;
        std::shared_ptr<Widget> _target;
        uintptr_t _hashcode{};

        ~ActivityStateAction() override;

    protected:
        ActivityStateAction();

    private:
    };

    typedef std::shared_ptr<ActivityStateAction> ActivityStateActionPtr;
    typedef std::vector<ActivityStateActionPtr> ActivityStateActionPtrVec;
    typedef std::set<ActivityStateActionPtr, Comparator<ActivityStateAction>> ActivityStateActionPtrSet;


}

#endif //Action_H_

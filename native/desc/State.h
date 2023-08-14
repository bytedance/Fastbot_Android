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
#ifndef State_H_
#define State_H_

#include "Node.h"
#include "../Base.h"
#include "Action.h"
#include "Widget.h"
#include "Element.h"
#include "ActionFilter.h"
#include <vector>


namespace fastbotx {


// State, StateKey
    class State : public Node, public PriorityNode, public HashNode {
    public:

        ActivityStateActionPtr getBackAction() const { return this->_backAction; }

        stringPtr getActivityString() const { return this->_activity; }

        //  implements
        std::string toString() const override;

        uintptr_t hash() const override;

        virtual ~State();

        // for algorithm
        int countActionPriority(const ActionFilterPtr &filter, bool includeBack) const;

        const ActivityStateActionPtrVec &getActions() const { return this->_actions; }

        ActivityStateActionPtrVec targetActions() const;

        ActivityStateActionPtr greedyPickMaxQValue(const ActionFilterPtr &filter) const;

        ActivityStateActionPtr randomPickUnvisitedAction() const;

        ActivityStateActionPtr randomPickAction(const ActionFilterPtr &filter) const;

        ActivityStateActionPtr resolveAt(ActivityStateActionPtr action, time_t t);

        bool containsTarget(const WidgetPtr &widget) const;

        bool isSaturated(const ActivityStateActionPtr &action) const;

        void setPriority(int p) { this->_priority = p; }

        bool operator<(const State &state) const;

        bool operator==(const State &state) const;

        virtual void clearDetails();

        virtual void fillDetails(const std::shared_ptr<State> &copy);

        bool hasNoDetail() const { return this->_hasNoDetail; }

        FuncGetID(State);

    protected:
        State();

        explicit State(stringPtr activityName);

        ///
        /// \param mergeWidgets
        /// \return
        int mergeWidgetAndStoreMergedOnes(WidgetPtrSet &mergeWidgets);

        ///
        /// \param parentWidget
        /// \param elem
        virtual void buildFromElement(WidgetPtr parentWidget, ElementPtr elem);

        ///
        /// \param filter
        /// \param includeBack
        /// \return
        ActivityStateActionPtr
        randomPickAction(const ActionFilterPtr &filter, bool includeBack) const;

        ///
        /// \param filter
        /// \param includeBack
        /// \param index
        /// \return
        ActivityStateActionPtr
        pickAction(const ActionFilterPtr &filter, bool includeBack, int index) const;


        uintptr_t _hashcode{}; //
        stringPtr _activity; //
        RectPtr _rootBounds; //
        ActivityStateActionPtrVec _actions; //
        WidgetPtrVec _widgets; //
        WidgetPtrVecMap _mergedWidgets; //

        bool _hasNoDetail; //
        static RectPtr _sameRootBounds; //
        ActivityStateActionPtr _backAction; //
    private:
        static std::shared_ptr<State> create(ElementPtr elem, stringPtr activityName);

        PropertyIDPrefix(State);

    };

    typedef std::shared_ptr<State> StatePtr;
    typedef std::vector<StatePtr> StatePtrVec;
    typedef std::set<StatePtr, Comparator<State>> StatePtrSet;

}

#endif //State_H_

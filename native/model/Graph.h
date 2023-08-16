/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef  Graph_H_
#define  Graph_H_

#include "State.h"
#include "Base.h"
#include "Action.h"
#include <map>

namespace fastbotx {

    typedef std::map<WidgetPtr, ActivityStateActionPtrSet, Comparator<Widget>> ModelActionPtrWidgetMap;
    typedef std::map<std::string, StatePtrSet> StatePtrStrMap;

    struct ActionCounter {
    private:

        // Enum Act count
        long actCount[ActionType::ActTypeSize];
        long total;

    public:
        ActionCounter()
                : actCount{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, total(0) {
        }

        void countAction(const ActivityStateActionPtr &action) {
            actCount[action->getActionType()]++;
            total++;
        }

        long getTotal() const { return total; }
    };


    class GraphListener {
    public:
        virtual void onAddNode(StatePtr node) = 0;
    };


    typedef std::shared_ptr<GraphListener> GraphListenerPtr;
    typedef std::vector<GraphListenerPtr> GraphListenerPtrVec;

    class Graph : Node {
    public:
        Graph();

        inline size_t stateSize() const { return this->_states.size(); }

        time_t getTimestamp() const { return this->_timeStamp; }

        void addListener(const GraphListenerPtr &listener);

        // add state to graph, adjust the state or return a exists state
        StatePtr addState(StatePtr state);

        long getTotalDistri() const { return this->_totalDistri; }

        stringPtrSet getVisitedActivities() const { return this->_visitedActivities; };

        virtual ~Graph();

    protected:
        void notifyNewStateEvents(const StatePtr &node);


    private:
        void addActionFromState(const StatePtr &node);


        StatePtrSet _states;      // all of the states in the graph
        stringPtrSet _visitedActivities; // a string set containing all the visited activities
        std::map<std::string, std::pair<int, double>> _activityDistri;
        long _totalDistri; // the count of reaching or accessing states, which could be new states or a state accessed before
        ModelActionPtrWidgetMap _widgetActions; //  query actions based on widget info

        ActivityStateActionPtrSet _unvisitedActions;
        ActivityStateActionPtrSet _visitedActions;

        ActionCounter _actionCounter;
        GraphListenerPtrVec _listeners;
        time_t _timeStamp;

        const static std::pair<int, double> _defaultDistri;
    };

    typedef std::shared_ptr<Graph> GraphPtr;

}

#endif  // Graph_H_

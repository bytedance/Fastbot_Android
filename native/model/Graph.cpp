/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef  Graph_CPP_
#define  Graph_CPP_


#include "Graph.h"
#include "../utils.hpp"
#include <vector>


namespace fastbotx {


    Graph::Graph()
            : _totalDistri(0), _timeStamp(0) {

    }

// first one describing the count of how many times that the state is visited.
// the second one, describing the percentage of times that this state been accessed over that of all the states.
    const std::pair<int, double> Graph::_defaultDistri = std::make_pair(0, 0.0);

    StatePtr Graph::addState(StatePtr state) {
        auto activity = state->getActivityString(); // get the activity name(activity class name) of this new state
        auto ifStateExists = this->_states.find(state); // try to find state in state caches
        if (ifStateExists ==
            this->_states.end()) // if this is a brand-new state, emplace this state inside _states cache
        {
            state->setId((int) this->_states.size());
            this->_states.emplace(state);
        } else {
            if ((*ifStateExists)->hasNoDetail()) {
                (*ifStateExists)->fillDetails(state);
            }
            state = *ifStateExists;
        }

        this->notifyNewStateEvents(state);

        this->_visitedActivities.emplace(
                activity); // add this activity name to this set, and in this set, every name is unique.
        this->_totalDistri++;
        std::string activityStr = *(activity.get());
        if (this->_activityDistri.find(activityStr) ==
            this->_activityDistri.end()) // try to find this activity name in the map of recording how many times one activity been accessed.
        {
            this->_activityDistri[activityStr] = _defaultDistri; // if not found, use the default pair of (0, 0) to initialize.
        }
        this->_activityDistri[activityStr].first++;
        this->_activityDistri[activityStr].second =
                1.0 * this->_activityDistri[activityStr].first / this->_totalDistri;
        addActionFromState(state);
        return state;
    }


    void Graph::notifyNewStateEvents(const StatePtr &node) {
        for (const auto &listener: this->_listeners) {
            listener->onAddNode(node);
        }
    }

    void Graph::addListener(const GraphListenerPtr &listener) {
        this->_listeners.emplace_back(listener);
    }

    void Graph::addActionFromState(const StatePtr &node) {
        auto nodeActions = node->getActions();
        for (const auto &action: nodeActions) {
            auto itervisted = this->_visitedActions.find(action);
            bool visitedadd = itervisted != this->_visitedActions.end();
            auto iterunvisited = this->_unvisitedActions.find(action);
            bool unvisitedadd = !visitedadd && iterunvisited != this->_unvisitedActions.end();
            if (visitedadd || unvisitedadd) {
                action->setId((visitedadd ? (*itervisted)->getIdi() : (*iterunvisited)->getIdi()));
            } else {
                action->setId((int) this->_actionCounter.getTotal());
                this->_actionCounter.countAction(action);
            }

            if (!visitedadd && action->isVisited())
                this->_visitedActions.emplace(action);

            if (!unvisitedadd && !action->isVisited())
                this->_unvisitedActions.emplace(action);
        }
        BDLOG("unvisited action: %zu, visited action %zu", this->_unvisitedActions.size(),
              this->_visitedActions.size());
    }

    Graph::~Graph() {
        this->_states.clear();
        this->_unvisitedActions.clear();
        this->_widgetActions.clear();
    }

}

#endif //Graph_CPP_

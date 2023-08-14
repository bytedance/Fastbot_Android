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
#ifndef State_CPP_
#define State_CPP_

#include "../Base.h"
#include  "State.h"
#include "../utils.hpp"
#include "ActionFilter.h"
#include <regex>
#include <map>
#include <algorithm>
#include <utility>
#include <cmath>

namespace fastbotx {

    State::State()
            : Node(), _hasNoDetail(false) {
    }

    State::State(stringPtr activityName)
            : Node(), _activity(std::move(activityName)), _hasNoDetail(false) {
        BLOG("create state");
    }

    int State::mergeWidgetAndStoreMergedOnes(WidgetPtrSet &mergeWidgets) {
        int mergedWidgetCount = 0;
        if (STATE_MERGE_DETAIL_TEXT && !this->_widgets.empty()) {
            for (const auto &widgetPtr: this->_widgets) {
                auto noMerged = mergeWidgets.emplace(widgetPtr).second;
                if (!noMerged) {
                    uintptr_t h = widgetPtr->hash();
                    mergedWidgetCount++;
                    if (this->_mergedWidgets.find(h) == this->_mergedWidgets.end()) {
                        WidgetPtrVec tempWidgetVector;
                        tempWidgetVector.emplace_back(widgetPtr);
                        this->_mergedWidgets.emplace(h, tempWidgetVector);
                    } else {
                        this->_mergedWidgets.at(h).emplace_back(widgetPtr);
                    }
                }
            }
        }
        return mergedWidgetCount;
    }

    StatePtr State::create(ElementPtr elem, stringPtr activityName) {
        StatePtr sharedPtr = std::shared_ptr<State>(new State(std::move(activityName)));
        sharedPtr->buildFromElement(nullptr, std::move(elem));
        uintptr_t activityHash =
                (std::hash<std::string>{}(*(sharedPtr->_activity.get())) * 31U) << 5;
        WidgetPtrSet mergedWidgets;
        int mergedWidgetCount = sharedPtr->mergeWidgetAndStoreMergedOnes(mergedWidgets);
        if (mergedWidgetCount != 0) {
            BDLOG("build state merged  %d widget", mergedWidgetCount);
            sharedPtr->_widgets.assign(mergedWidgets.begin(), mergedWidgets.end());
        }
        activityHash ^= (combineHash<Widget>(sharedPtr->_widgets, STATE_WITH_WIDGET_ORDER) << 1);
        sharedPtr->_hashcode = activityHash;
        // build State module actions
        for (auto w: sharedPtr->_widgets) {
            if (w->getBounds() == nullptr) {
                BLOGE("NULL Bounds happened");
                continue;
            }
            for (ActionType act: w->getActions()) {
                ActivityStateActionPtr modelAction = std::make_shared<ActivityStateAction>(
                        sharedPtr, w, act);
                sharedPtr->_actions.emplace_back(modelAction);
            }
        }
        sharedPtr->_backAction = std::make_shared<ActivityStateAction>(sharedPtr, nullptr,
                                                                       ActionType::BACK);
        sharedPtr->_actions.emplace_back(sharedPtr->_backAction);

        return sharedPtr;
    }

    bool State::isSaturated(const ActivityStateActionPtr &action) const {
        if (!action->requireTarget()) {
            return action->isVisited();
        }
        if (nullptr != action->getTarget()) {
            uintptr_t h = action->getTarget()->hash();
            if (this->_mergedWidgets.find(h) != this->_mergedWidgets.end()) {
                return action->getVisitedCount() > (int) this->_mergedWidgets.at(h).size();
            }
        }
        return action->getVisitedCount() >= 1;
    }

    RectPtr State::_sameRootBounds = std::make_shared<Rect>();

    void State::buildFromElement(WidgetPtr parentWidget, ElementPtr elem) {
        if (elem->getParent().expired() && !(elem->getBounds()->isEmpty())) {
            if (_sameRootBounds.get()->isEmpty() && elem) {
                _sameRootBounds = elem->getBounds();
            }
            if (equals(_sameRootBounds, elem->getBounds())) {
                this->_rootBounds = _sameRootBounds;
            } else
                this->_rootBounds = elem->getBounds();
        }
        WidgetPtr widget = nullptr;
        widget = std::make_shared<Widget>(parentWidget, elem);
        this->_widgets.emplace_back(widget);
        for (const auto &childElement: elem->getChildren()) {
            buildFromElement(widget, childElement);
        }
    }

    uintptr_t State::hash() const {
        return this->_hashcode;
    }

    bool State::operator<(const State &state) const {
        return this->hash() < state.hash();
    }

    State::~State() {
        this->_activity.reset();
        this->_actions.clear();
        this->_backAction = nullptr;
        this->_widgets.clear();

        this->_mergedWidgets.clear();
    }


    void State::clearDetails() {
        for (auto const &widget: this->_widgets) {
            widget->clearDetails();
        }
        this->_mergedWidgets.clear();
        _hasNoDetail = true;
    }

    void State::fillDetails(const std::shared_ptr<State> &copy) {
        for (auto widgetPtr: this->_widgets) {
            auto widgetIterator = std::find_if(copy->_widgets.begin(), copy->_widgets.end(),
                                               [&widgetPtr](const WidgetPtr &cw) {
                                                   return *(cw.get()) == *widgetPtr;
                                               });
            if (widgetIterator != copy->_widgets.end()) {
                widgetPtr->fillDetails(*widgetIterator);
            } else {
                LOGE("ERROR can not refill widget");
            }
        }
        for (const auto &miter: this->_mergedWidgets) {
            auto mkw = copy->_mergedWidgets.find(miter.first);
            if (mkw == copy->_mergedWidgets.end())
                continue;
            for (auto widgetPtr: miter.second) {
                auto widgetIterator = std::find_if((*mkw).second.begin(), (*mkw).second.end(),
                                                   [&widgetPtr](const WidgetPtr &cw) {
                                                       return *(cw.get()) == *widgetPtr;
                                                   });
                if (widgetIterator != (*mkw).second.end()) {
                    widgetPtr->fillDetails(*widgetIterator);
                }
            }

        }
        _hasNoDetail = false;
    }

    std::string State::toString() const {
        std::string ret("{state: " + std::to_string(this->hash()) + "\n    widgets: \n");
        for (auto const &widget: this->_widgets) {
            ret += "   " + widget->toString() + "\n";
        }
        ret += ("action: \n");
        for (auto const &action: this->_actions) {
            ret += "   " + action->toString() + "\n";
        }
        return ret + "\n}";
    }


    // for algorithm
    int State::countActionPriority(const ActionFilterPtr &filter, bool includeBack) const {
        int totalP = 0;
        for (const auto &action: this->_actions) {
            if (!includeBack && action->isBack()) {
                continue;
            }
            if (filter->include(action)) {
                int fp = filter->getPriority(action);
                if (fp <= 0) {
                    BDLOG("Error: Action should has a positive priority, but we get %d", fp);
                    return -1;
                }
                totalP += fp;
            }
        }
        return totalP;
    }

    ActivityStateActionPtrVec State::targetActions() const {
        ActivityStateActionPtrVec retV;
        ActionFilterPtr filter = targetFilter; //(ActionFilterPtr(new ActionFilterTarget());)
        for (const auto &a: this->_actions) {
            if (filter->include(a))
                retV.emplace_back(a);
        }
        return retV;
    }

    ActivityStateActionPtr State::greedyPickMaxQValue(const ActionFilterPtr &filter) const {
        ActivityStateActionPtr retA;
        long maxvalue = 0;
        for (const auto &m: this->_actions) {
            if (!filter->include(m))
                continue;
            if (filter->getPriority(m) > maxvalue) {
                maxvalue = filter->getPriority(m);
                retA = m;
            }
        }
        return retA;
    }

    ActivityStateActionPtr State::randomPickAction(const ActionFilterPtr &filter) const {
        return this->randomPickAction(filter, true);
    }

    ActivityStateActionPtr
    State::randomPickAction(const ActionFilterPtr &filter, bool includeBack) const {
        int total = this->countActionPriority(filter, includeBack);
        if (total == 0)
            return nullptr;
        srand((uint32_t) (int) time(
                nullptr)); //// this->_hashcode); // srand with hash, one sequence
        int index = rand() % total;
        return pickAction(filter, includeBack, index);
    }

    ActivityStateActionPtr
    State::pickAction(const ActionFilterPtr &filter, bool includeBack, int index) const {
        int ii = index;
        for (auto action: this->_actions) {
            if (!includeBack && action->isBack())
                continue;
            if (filter->include(action)) {
                int p = filter->getPriority(action);
                if (p > ii)
                    return action;
                else
                    ii = ii - p;
            }
        }
        BDLOG("%s", "ERROR: action filter is unstable");
        return nullptr;
    }

    ActivityStateActionPtr State::randomPickUnvisitedAction() const {
        ActivityStateActionPtr action = this->randomPickAction(enableValidUnvisitedFilter, false);
        if (action == nullptr && enableValidUnvisitedFilter->include(getBackAction())) {
            action = getBackAction();
        }
        return action;
    }


    ActivityStateActionPtr State::resolveAt(ActivityStateActionPtr action, time_t t) {
        if (action->getTarget() == nullptr)
            return action;
        uintptr_t h = action->getTarget()->hash();
        auto targetWidgets = this->_mergedWidgets.find(h);
        if (targetWidgets == this->_mergedWidgets.end()) {
            return action;
        }
        int total = (int) (this->_mergedWidgets.at(h).size());
        int index = action->getVisitedCount() % total;
        BLOG("resolve a merged widget %d/%d for action %s", index, total, action->getId().c_str());
        action->setTarget(this->_mergedWidgets.at(h)[index]);
        return action;
    }

    bool State::containsTarget(const WidgetPtr &widget) const {
        for (const auto &w: this->_widgets) {
            if (equals(w, widget))
                return true;
        }
        return false;
    }

    PropertyIDPrefixImpl(State, "g0s");

    bool State::operator==(const State &state) const {
        return this->hash() == state.hash();
    }


} // namespace fastbot


#endif // State_CPP_

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
#ifndef ReuseState_CPP_
#define ReuseState_CPP_

#include "ReuseState.h"

#include <utility>
#include "RichWidget.h"
#include "ActivityNameAction.h"
#include "../utils.hpp"
#include "ActionFilter.h"

namespace fastbotx {


    ReuseState::ReuseState()
    = default;

    ReuseState::ReuseState(stringPtr activityName)
            : ReuseState() {
        this->_activity = std::move(activityName);
        this->_hasNoDetail = false;
    }

    void ReuseState::buildBoundingBox(const ElementPtr &element) {
        if (element->getParent().expired() &&
            !(element->getBounds() && element->getBounds()->isEmpty())) {
            if (_sameRootBounds.get()->isEmpty() && element) {
                _sameRootBounds = element->getBounds();
            }
            if (equals(_sameRootBounds, element->getBounds())) {
                this->_rootBounds = _sameRootBounds;
            } else
                this->_rootBounds = element->getBounds();
        }
    }

    void ReuseState::buildStateFromElement(WidgetPtr parentWidget, ElementPtr element) {
        buildBoundingBox(element);
        // use RichWidget build the states
        WidgetPtr widget = std::make_shared<RichWidget>(parentWidget, element);
        this->_widgets.emplace_back(widget);
        for (const auto &childElement: element->getChildren()) {
            buildFromElement(widget, childElement);
        }
    }

    void ReuseState::buildFromElement(WidgetPtr parentWidget, ElementPtr elem) {
        buildBoundingBox(elem);
        auto element = std::dynamic_pointer_cast<Element>(elem);
        WidgetPtr widget = std::make_shared<Widget>(parentWidget, element);
        this->_widgets.emplace_back(widget);
        for (const auto &childElement: elem->getChildren()) {
            buildFromElement(widget, childElement);
        }
    }

/// @brief according to the element, or XML of this page, and the activity name,
///        create a state and the actions in this page according to the widgets inside this page.
/// @param element XML of this page
/// @param activityName activity name of this page
/// @return a newly created ReuseState according to this page
    ReuseStatePtr ReuseState::create(const ElementPtr &element, const stringPtr &activityName) {
        ReuseStatePtr statePointer = std::shared_ptr<ReuseState>(new ReuseState(activityName));
        statePointer->buildState(element);
        return statePointer;
    }

    void ReuseState::buildState(const ElementPtr &element) {
        buildStateFromElement(nullptr, element);
        mergeWidgetsInState();
        buildHashForState();
        buildActionForState();
    }

    void ReuseState::buildHashForState() {
        //build hash
        std::string activityString = *(_activity.get());
        uintptr_t activityHash = (std::hash<std::string>{}(activityString) * 31U) << 5;
        activityHash ^= (combineHash<Widget>(_widgets, STATE_WITH_WIDGET_ORDER) << 1);
        _hashcode = activityHash;
    }

    void ReuseState::buildActionForState() {
        for (const auto &widget: _widgets) {
            if (widget->getBounds() == nullptr) {
                BLOGE("NULL Bounds happened");
                continue;
            }
            for (auto action: widget->getActions()) {
                ActivityNameActionPtr activityNameAction = std::shared_ptr<ActivityNameAction>
                        (new ActivityNameAction(getActivityString(), widget, action));
                // Appends a new element to the end of the container.
                // emplace_back() constructs the object in-place at the end of the list,
                // potentially improving performance by avoiding a copy operation,
                // while push_back() adds a copy of the object to the end of the list.
                _actions.emplace_back(activityNameAction);
            }
        }
        _backAction = std::make_shared<ActivityNameAction>(getActivityString(), nullptr,
                                                           ActionType::BACK);
        _actions.emplace_back(_backAction);
    }

    void ReuseState::mergeWidgetsInState() {
        WidgetPtrSet mergedWidgets;
        int mergedCount = mergeWidgetAndStoreMergedOnes(mergedWidgets);
        if (mergedCount != 0) {
            BDLOG("build state merged  %d widget", mergedCount);
            _widgets.assign(mergedWidgets.begin(), mergedWidgets.end());
        }
    }


} // namespace fastbot


#endif // ReuseState_CPP_

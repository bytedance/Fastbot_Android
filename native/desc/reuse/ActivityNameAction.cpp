/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef ActivityNameAction_CPP_
#define ActivityNameAction_CPP_

#include  "ActivityNameAction.h"

#include <utility>
#include "../utils.hpp"
#include "ActionFilter.h"

namespace fastbotx {

    ActivityNameAction::ActivityNameAction()
            : ActivityStateAction(), _activity(nullptr) {

    }

    ActivityNameAction::ActivityNameAction(stringPtr activity, const WidgetPtr &widget,
                                           ActionType act)
            : ActivityStateAction(nullptr, widget, act), _activity(std::move(activity)) {
        uintptr_t activityHashCode = std::hash<std::string>{}(*(_activity.get()));
        uintptr_t actionHashCode = std::hash<int>{}(this->getActionType());
        uintptr_t targetHash = nullptr != widget ? widget->hash() : 0x1;

        this->_hashcode = 0x9e3779b9 + (activityHashCode << 2) ^
                          (((actionHashCode << 6) ^ (targetHash << 1)) << 1);
    }

    ActivityNameAction::~ActivityNameAction()
    = default;

} // namespace fastbot


#endif // ActivityNameAction_CPP_

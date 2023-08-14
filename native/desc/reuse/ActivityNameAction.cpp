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

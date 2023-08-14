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
#ifndef ActionFilter_CPP_
#define ActionFilter_CPP_

#include "ActionFilter.h"
#include "State.h"

namespace fastbotx {
    bool ActionFilterValidUnSaturated::include(ActivityStateActionPtr action) const {
        bool ret = action->getEnabled() && action->isValid();
        ret = ret && !action->getState().expired();
        ret = ret && !action->getState().lock()->isSaturated(action);
        return ret;
    }

    ActionFilterPtr allFilter = ActionFilterPtr(new ActionFilterALL());
    ActionFilterPtr targetFilter = ActionFilterPtr(new ActionFilterTarget());
    ActionFilterPtr validFilter = ActionFilterPtr(new ActionFilterValid());
    ActionFilterPtr enableValidFilter = ActionFilterPtr(new ActionFilterEnableValid());
    ActionFilterPtr enableValidUnvisitedFilter = ActionFilterPtr(new ActionFilterUnvisitedValid());
    ActionFilterPtr enableValidUnSaturatedFilter = ActionFilterPtr(
            new ActionFilterValidUnSaturated());
    ActionFilterPtr enableValidValuePriorityFilter = ActionFilterPtr(
            new ActionFilterValidValuePriority());
    ActionFilterPtr validDatePriorityFilter = ActionFilterPtr(new ActionFilterValidDatePriority());

}
#endif

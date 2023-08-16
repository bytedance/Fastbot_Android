/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
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

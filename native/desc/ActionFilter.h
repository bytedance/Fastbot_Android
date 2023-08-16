/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef ActionFilter_H_
#define ActionFilter_H_

#include "Action.h"
#include "utils.hpp"
#include <memory>
#include <cmath>

namespace fastbotx {


    class ActionFilter {
    public:
        virtual bool include(ActivityStateActionPtr action) const = 0;

        virtual int getPriority(ActivityStateActionPtr action) const {
            return action->getPriority();
        }

        virtual ~ActionFilter() = default;
    };

    class ActionFilterALL : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return true;
        }
    };

    class ActionFilterTarget : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return action->requireTarget();
        }
    };

    class ActionFilterValid : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return action->isValid();
        }
    };

    class ActionFilterEnableValid : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return action->getEnabled() && action->isValid();
        }
    };

    class ActionFilterUnvisitedValid : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return action->getEnabled() && action->isValid() && !action->isVisited();
        }
    };

    class ActionFilterValidUnSaturated : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override;
    };

    class ActionFilterValidValuePriority : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            return (action->getEnabled() && action->isValid());
        }

        int getPriority(ActivityStateActionPtr action) const override {
            int pri = action->getPriority();
            if (!action->isBack()) {
                pri += (int) ceil(10 * action->getQValue());
            }
            return pri;
        }
    };

    class ActionFilterValidDatePriority : public ActionFilter {
    public:
        bool include(ActivityStateActionPtr action) const override {
            if (nullptr == action)
                return false;
            switch (action->getActionType()) {
                case ActionType::START:
                case ActionType::RESTART:
                case ActionType::CLEAN_RESTART:
                case ActionType::NOP:
                case ActionType::ACTIVATE:
                case ActionType::BACK:
                    return true;
                case ActionType::CLICK:
                case ActionType::LONG_CLICK:
                case ActionType::SCROLL_BOTTOM_UP:
                case ActionType::SCROLL_TOP_DOWN:
                case ActionType::SCROLL_LEFT_RIGHT:
                case ActionType::SCROLL_RIGHT_LEFT:
                case ActionType::SCROLL_BOTTOM_UP_N:
                    return action->getEnabled() && action->isValid() && !action->isEmpty();
                default:
                    BLOGE("Should not reach here");
                    return false;

            }
        }

    private:

    };


    typedef std::shared_ptr<ActionFilter> ActionFilterPtr;

    extern ActionFilterPtr allFilter;
    extern ActionFilterPtr targetFilter;
    extern ActionFilterPtr validFilter;
    extern ActionFilterPtr enableValidFilter;
    extern ActionFilterPtr enableValidUnvisitedFilter;
    extern ActionFilterPtr enableValidUnSaturatedFilter;
    extern ActionFilterPtr enableValidValuePriorityFilter;
    extern ActionFilterPtr validDatePriorityFilter;

}
#endif /* ActionFilter_H_ */

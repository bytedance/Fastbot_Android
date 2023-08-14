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

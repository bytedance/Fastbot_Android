/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef ActivityNameAction_H_
#define ActivityNameAction_H_

#include "Action.h"
#include "RichWidget.h"
#include <vector>


namespace fastbotx {

    class ActivityNameAction : public ActivityStateAction {
    public:
        ActivityNameAction(stringPtr activity, const WidgetPtr &widget, ActionType act);

        stringPtr getActivity() const { return this->_activity; }

        ~ActivityNameAction() override;

    protected:
        ActivityNameAction();

        stringPtr _activity;

    };

    typedef std::shared_ptr<ActivityNameAction> ActivityNameActionPtr;
    typedef std::vector<ActivityNameActionPtr> ActivityNameActionPtrVec;
    typedef std::set<ActivityNameActionPtr, Comparator<ActivityNameAction>> ActivityNameActionPtrSet;

}

#endif //ActivityNameAction_H_

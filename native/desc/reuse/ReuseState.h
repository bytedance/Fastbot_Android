/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef ReuseState_H_
#define ReuseState_H_

#include "State.h"
#include "RichWidget.h"
#include <vector>


namespace fastbotx {

// This class is for build a state which holds all RichWidgets and their associated actions and so on.
    class ReuseState : public State {
    public:
        static std::shared_ptr<ReuseState>
        create(const ElementPtr &element, const stringPtr &activityName);

    protected:
        virtual void buildStateFromElement(WidgetPtr parentWidget, ElementPtr element);

        virtual void buildHashForState();

        virtual void buildActionForState();

        virtual void mergeWidgetsInState();

        explicit ReuseState(stringPtr activityName);

        ReuseState();

        virtual void buildState(const ElementPtr &element);

        virtual void buildBoundingBox(const ElementPtr &element);

    private:
        void buildFromElement(WidgetPtr parentWidget, ElementPtr elem) override;
    };

    typedef std::shared_ptr<ReuseState> ReuseStatePtr;

}


#endif //ReuseState_H_

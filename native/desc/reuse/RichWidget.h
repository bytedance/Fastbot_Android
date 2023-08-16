/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef RichWidget_H_
#define RichWidget_H_

#include "Widget.h"

namespace fastbotx {

/// Use the actions, class name, resource id, text of itself (or its children)
/// to embed and generate hash code, to identify widget
    class RichWidget : virtual public Widget {
    public:
        /// Use the supported actions, class name, resource id, text of itself or
        /// its children of this widget for embedding and generating hash code,
        /// to identify single widget.
        /// \param parent parent widget of this widget
        /// \param element the XML info of this widget
        RichWidget(WidgetPtr parent, const ElementPtr &element);

        uintptr_t hash() const override;

        uintptr_t getActHashCode() const { return this->_widgetHashcode; }

    protected:
        RichWidget();

        uintptr_t _widgetHashcode{};

    private:
        /// Get Element valid text. If parent widget are not clickable, get children's valid text
        /// \param element
        /// \return valid text from widget or its children or offspring.
        std::string getValidTextFromWidgetAndChildren(const ElementPtr &element) const;
    };

}


#endif //RichWidget_H_

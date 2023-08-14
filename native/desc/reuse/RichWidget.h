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

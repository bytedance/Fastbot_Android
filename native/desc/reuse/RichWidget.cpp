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
#ifndef RichWidget_CPP_
#define RichWidget_CPP_


#include "RichWidget.h"
#include "../utils.hpp"
#include <algorithm>
#include <utility>

namespace fastbotx {


    RichWidget::RichWidget(WidgetPtr parent, const ElementPtr &element)
            : Widget(std::move(parent), element) {
        uintptr_t hashcode1 = std::hash<std::string>{}(this->_clazz);
        uintptr_t hashcode2 = std::hash<std::string>{}(this->_resourceID);
        uintptr_t hashcode3 = 0x1;
        for (int i: this->getActions()) {
            hashcode3 ^= (127U * std::hash<int>{}(i));
        }
        this->_widgetHashcode = ((hashcode1 ^ (hashcode2 << 4)) >> 2) ^ ((127U * hashcode3 << 1));
        std::string elementText = this->getValidTextFromWidgetAndChildren(element);
        if (!elementText.empty())
            this->_widgetHashcode ^= (0x79b9 + (std::hash<std::string>{}(elementText) << 1));

    }

    std::string RichWidget::getValidTextFromWidgetAndChildren(const ElementPtr &element) const {
        std::string txt = element->validText;
        if (txt.empty()) {
            for (const auto &child: element->getChildren()) {
                txt = this->getValidTextFromWidgetAndChildren(child);
                if (!txt.empty()) {
                    return txt;
                }
            }
        }
        return txt;
    }

    RichWidget::RichWidget()
            : Widget() {

    }

    uintptr_t RichWidget::hash() const {
        return getActHashCode();
    }


}

#endif //RichWidget_CPP_

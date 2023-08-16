/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Widget_CPP_
#define Widget_CPP_


#include "Widget.h"
#include "../utils.hpp"
#include "Preference.h"
#include <algorithm>
#include <utility>

namespace fastbotx {

    Widget::Widget() = default;

    const auto ifCharIsDigitOrBlank = [](const char &c) -> bool {
        return c == ' ' || (c >= '0' && c <= '9');
    };


    Widget::Widget(std::shared_ptr<Widget> parent, const ElementPtr &element) {
        this->_parent = std::move(parent);
        this->initFormElement(element);
        // remove digits or blank space in string
        auto removeIterator = this->_text.erase(
                std::remove_if(this->_text.begin(), this->_text.end(), ifCharIsDigitOrBlank),
                this->_text.end());
        this->_text = std::string(this->_text.begin(), removeIterator);
        if (STATE_WITH_TEXT || Preference::inst()->isForceUseTextModel()) {
            bool overMaxLen = this->_text.size() > STATE_TEXT_MAX_LEN;
            this->_text = this->_text.substr(0, STATE_TEXT_MAX_LEN * 4);
            int cutLength = STATE_TEXT_MAX_LEN;
            if (this->_text.length() > cutLength && isZhCn(this->_text[STATE_TEXT_MAX_LEN])) {
                int ci = 0;
                for (; ci < cutLength; ci++) {
                    if (isZhCn(this->_text[ci])) {
                        ci += 2;
                    }
                }
                cutLength = ci;
            }

            this->_text = this->_text.substr(0, cutLength);
            if (!overMaxLen)
                this->_hashcode ^= (0x79b9 + (std::hash<std::string>{}(this->_text) << 5));
        }

        if (STATE_WITH_INDEX) {
            this->_hashcode ^= ((0x79b9 + (std::hash<int>{}(this->_index) << 6)) << 1);
        }
    }

    void Widget::initFormElement(const ElementPtr &element) {
        if (element->getCheckable())
            enableOperate(OperateType::Checkable);
        if (element->getEnable())
            enableOperate(OperateType::Enable);
        if (element->getClickable())
            enableOperate(OperateType::Clickable);
        if (element->getScrollable())
            enableOperate(OperateType::Scrollable);
        if (element->getLongClickable()) {
            enableOperate(OperateType::LongClickable);
            this->_actions.insert(ActionType::LONG_CLICK);
        }
        if (this->hasOperate(OperateType::Checkable) ||
            this->hasOperate(OperateType::Clickable)) {
            this->_actions.insert(ActionType::CLICK);
        }

        ScrollType scrollType = element->getScrollType();
        switch (scrollType) {
            case ScrollType::NONE:
                break;
            case ScrollType::ALL:
                this->_actions.insert(ActionType::SCROLL_BOTTOM_UP);
                this->_actions.insert(ActionType::SCROLL_TOP_DOWN);
                this->_actions.insert(ActionType::SCROLL_LEFT_RIGHT);
                this->_actions.insert(ActionType::SCROLL_RIGHT_LEFT);
                break;
            case ScrollType::Horizontal:
                this->_actions.insert(ActionType::SCROLL_LEFT_RIGHT);
                this->_actions.insert(ActionType::SCROLL_RIGHT_LEFT);
                break;
            case ScrollType::Vertical:
                this->_actions.insert(ActionType::SCROLL_BOTTOM_UP);
                this->_actions.insert(ActionType::SCROLL_TOP_DOWN);
                break;
            default:
                break;
        }

        if (this->hasAction()) {
            this->_clazz = (element->getClassname());
            this->_isEditable = ("android.widget.EditText" == this->_clazz
                                 || "android.inputmethodservice.ExtractEditText" == this->_clazz
                                 || "android.widget.AutoCompleteTextView" == this->_clazz
                                 || "android.widget.MultiAutoCompleteTextView" == this->_clazz);

            if (SCROLL_BOTTOM_UP_N_ENABLE && (0 == this->_clazz.compare("android.widget.ListView")
                                              || 0 == this->_clazz.compare(
                    "android.support.v7.widget.RecyclerView")
                                              || 0 == this->_clazz.compare(
                    "androidx.recyclerview.widget.RecyclerView"))) {
                this->_actions.insert(ActionType::SCROLL_BOTTOM_UP_N);
            }
            this->_resourceID = (element->getResourceID());
        }
        if (element->getBounds())
            this->_bounds = element->getBounds();
        this->_index = element->getIndex();
        this->_enabled = element->getEnable();
        this->_text = element->getText();
        this->_contextDesc = (element->getContentDesc());
        // compute for only 1 time
        uintptr_t hashcode1 = std::hash<std::string>{}(this->_clazz);
        uintptr_t hashcode2 = std::hash<std::string>{}(this->_resourceID);
        uintptr_t hashcode3 = std::hash<int>{}(this->_operateMask);
        uintptr_t hashcode4 = std::hash<int>{}(scrollType);

        this->_hashcode = ((hashcode1 ^ (hashcode2 << 4)) >> 2) ^
                          (((127U * hashcode3 << 1) ^ (256U * hashcode4 << 3)) >> 1);
    }

    bool Widget::isEditable() const {
        return this->_isEditable;
    }

    void Widget::clearDetails() {
        this->_clazz.clear();
        this->_text.clear();
        this->_contextDesc.clear();
        this->_resourceID.clear();
        this->_bounds = Rect::RectZero;
    }

    void Widget::fillDetails(const std::shared_ptr<Widget> &copy) {
        this->_text = copy->_text;
        this->_clazz = copy->_clazz;
        this->_contextDesc = copy->_contextDesc;
        this->_resourceID = copy->_resourceID;
        this->_bounds = copy->getBounds();
        this->_enabled = copy->_enabled;
    }

    std::string Widget::toString() const {
        return this->toXPath();
    }


    std::string Widget::toXPath() const {
        if (this->_text.empty() && this->_clazz.empty()
            && this->_resourceID.empty()) {
            BDLOG("widget detail has been clear");
            return "";
        }

        std::stringstream stringStream;
        stringStream << "{xpath: /*" <<
                     "[@class=\"" << this->_clazz << "\"]" <<
                     "[@resource-id=\"" << this->_resourceID << "\"]" <<
                     "[@text=\"" << this->_text << "\"]" <<
                     "[@content-desc=\"" << this->_contextDesc << "\"]" <<
                     "[@index=" << this->_index << "]" <<
                     "[@bounds=\"" << this->_bounds->toString() << "\"]}";
        return stringStream.str();
    }

    std::string Widget::buildFullXpath() const {
        std::string fullXpathString = this->toXPath();
        std::shared_ptr<Widget> parent = _parent;
        while (parent) {
            std::string parentXpath = parent->toXPath();
            parentXpath.append(fullXpathString);
            fullXpathString = parentXpath;
            parent = parent->_parent;
        }
        return fullXpathString;
    }

    Widget::~Widget() {
        this->_actions.clear();
        this->_parent = nullptr;
    }

    uintptr_t Widget::hash() const {
        return _hashcode;
    }

}

#endif //Widget_CPP_

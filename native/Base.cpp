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
#ifndef BASE_CPP_
#define BASE_CPP_

#include "Base.h"
#include "utils.hpp"
#include <algorithm>

namespace fastbotx {

    /// The mapping table for converting string to ActionType
    const std::string actName[ActTypeSize] = {
            "CRASH",
            "FUZZ",
            "START",
            "RESTART",
            "CLEAN_RESTART",
            "NOP",
            "ACTIVATE",
            "BACK",
            "FEED",
            "CLICK",
            "LONG_CLICK",
            "SCROLL_TOP_DOWN",
            "SCROLL_BOTTOM_UP",
            "SCROLL_LEFT_RIGHT",
            "SCROLL_RIGHT_LEFT",
            "SCROLL_BOTTOM_UP_N",
            "SHELL_EVENT",
            "HOVER"
    };

    const std::string ScrollTypeName[] = {
            "all",
            "horizontal",
            "vertical",
            "none",
            "verticaleries"
    };

    /// According to the string of action type, convert it to the corresponding value of ActionType
    /// \param actionTypeString String of action type to be converted
    /// \return The converted value of ActionType
    ActionType stringToActionType(const std::string &actionTypeString) {
        for (int i = 0; i < ActTypeSize; i++) {
            if (actName[i] == actionTypeString)
                return (ActionType) i;
        }
        return ActionType::ActTypeSize;
    }

    ScrollType stringToScrollType(const std::string &str) {
        for (int i = 0; i < ScrollTypeSize; i++) {
            if (ScrollTypeName[i] == str)
                return (ScrollType) i;
        }
        return ScrollType::NONE;
    }

    PriorityNode::PriorityNode()
            : _priority(0) {

    }

    Rect::Rect() {
        this->top = 0;
        this->bottom = 0;
        this->right = 0;
        this->left = 0;
    }

    Rect::Rect(const Rect &rect) {
        this->top = rect.top;
        this->bottom = rect.bottom;
        this->right = rect.right;
        this->left = rect.left;
    }

    Rect::Rect(int left, int top, int right, int bottom) {
        this->top = top;
        this->bottom = bottom;
        this->right = right;
        this->left = left;
    }

    bool Rect::isEmpty() const {
        return this->left >= this->right || this->top >= this->bottom;
    }

    Point Rect::center() const {
        return {(int) ((double) (this->top) + 0.5f * (double) (this->bottom - this->top)),
                (int) ((double) this->left + 0.5f * (double) (this->right - this->left))};
    }

    bool Rect::contains(const Point &point) const {
        return point.x >= this->left && point.x <= this->right
               && point.y >= this->top && point.y <= this->bottom;
    }

    uintptr_t Rect::hash() const {
        return (31U * std::hash<int>{}(top) << 1 ^ std::hash<int>{}(bottom) << 2) ^
               ((std::hash<int>{}(left) << 1 ^ 127U * std::hash<int>{}(right) << 2) >> 1);
    }

    std::string Rect::toString() const {
        std::stringstream strs;
        strs << "[" << this->left << "," << this->top
             << "][" << this->right << "," << this->bottom << "]";
        return strs.str();
    }

    bool Rect::operator==(const Rect &node) const {
        return this->left == node.left &&
               this->top == node.top &&
               this->right == node.right &&
               this->bottom == node.bottom;
    }


    Rect &Rect::operator=(const Rect &node) {
        this->left = node.left;
        this->top = node.top;
        this->right = node.right;
        this->bottom = node.bottom;
        return *this;
    }

    const RectPtr Rect::RectZero = std::make_shared<Rect>();
    std::vector<std::shared_ptr<Rect>> Rect::_rectPool;

    RectPtr Rect::getRect(const RectPtr &rect) {
        if (nullptr == rect || rect->isEmpty()) {
            return RectZero;
        }
        return rect;
    }

    Point::Point() {
        this->x = 0;
        this->y = 0;
    }

    Point::Point(const Point &point) {
        this->x = point.x;
        this->y = point.y;
    }

    Point::Point(int x, int y) {
        this->x = x;
        this->y = y;
    }

    uintptr_t Point::hash() const {
        return (31U * std::hash<int>{}(x) << 1) ^
               ((127U * std::hash<int>{}(y) << 2) >> 1);
    }

    bool Point::operator==(const Point &node) const {
        return this->x == node.x && this->y == node.y;
    }


    Point &Point::operator=(const Point &node) {
        this->x = node.x;
        this->y = node.y;
        return *this;
    }

}
#endif //BASE_CPP_

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
#ifndef Operate_H_
#define Operate_H_

#include <string>
#include "../Base.h"

namespace fastbotx {

    /// Class for converting model generated operation to operation that device could understand.
    class DeviceOperateWrapper {
    public:
        ActionType act; // Action type
        Rect pos;
        std::string sid;
        std::string aid;
        float throttle;
        int waitTime;
        bool editable{};
        bool allowFuzzing{true};
        bool clear{};
        bool adbInput{};
        std::string name;

        DeviceOperateWrapper();

        DeviceOperateWrapper(const DeviceOperateWrapper &opt);

        explicit DeviceOperateWrapper(const std::string &optJsonStr);

        DeviceOperateWrapper &operator=(const DeviceOperateWrapper &node);

        std::string setText(const std::string &text);

        const std::string &getText() const { return this->_text; }

        std::string toString() const;

        virtual ~DeviceOperateWrapper() = default;

        static std::shared_ptr<DeviceOperateWrapper> OperateNop;
    protected:
        bool rawInput{};
        std::string _text;
        std::string extra0;
        std::string jAction;
    };

    typedef std::shared_ptr<DeviceOperateWrapper> OperatePtr;

}

#endif

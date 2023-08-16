/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
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

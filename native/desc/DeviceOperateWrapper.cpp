/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#include "DeviceOperateWrapper.h"
#include "utils.hpp"
#include "../Base.h"
#include "json.hpp"

namespace fastbotx {


    DeviceOperateWrapper::DeviceOperateWrapper()
            : act(ActionType::NOP), throttle(0), waitTime(0), editable(false), clear(false),
              adbInput(false), rawInput(false) {

    }

    DeviceOperateWrapper::DeviceOperateWrapper(const DeviceOperateWrapper &opt) {
        this->act = opt.act;
        this->pos = opt.pos;
        this->sid = opt.sid;
        this->aid = opt.aid;
        this->waitTime = opt.waitTime;
        this->throttle = opt.throttle;
        this->_text = opt._text;
        this->extra0 = opt.extra0;
        this->name = opt.name;
    }

    DeviceOperateWrapper &DeviceOperateWrapper::operator=(const DeviceOperateWrapper &opt) {
        this->act = opt.act;
        this->pos = opt.pos;
        this->sid = opt.sid;
        this->aid = opt.aid;
        this->waitTime = opt.waitTime;
        this->throttle = opt.throttle;
        this->_text = opt._text;
        this->extra0 = opt.extra0;
        this->name = opt.name;
        return *this;
    }

    std::string DeviceOperateWrapper::setText(const std::string &text) {
        this->_text = text;
        if (this->_text.length() > 1000) {
            this->_text = this->_text.substr(0, 999);
        }
        if (!this->editable) {
            LOGW("set text to a none editable node %s", this->toString().c_str());
        }
        return this->_text;
    }

#define BOOL_TO_STR(b) (b?"true":"false")


    DeviceOperateWrapper::DeviceOperateWrapper(const std::string &optJsonStr)
            : DeviceOperateWrapper() {
        std::string ret;
        try {
            nlohmann::json retJson = nlohmann::json::parse(optJsonStr);
            std::string actionStr = getJsonValue<std::string>(retJson, "act", "");
            ActionType jact = stringToActionType(actionStr);
            if (jact == ActionType::ActTypeSize) {
                BLOG("Error action Type: %s", actionStr.c_str());
                return;
            }
            this->act = jact;

            if (retJson.contains("pos")) {
                nlohmann::json positionArray = retJson["pos"];
                if (positionArray.size() >= 4) {
                    this->pos.left = positionArray[0];
                    this->pos.top = positionArray[1];
                    this->pos.right = positionArray[2];
                    this->pos.bottom = positionArray[3];
                } else if (jact > ActionType::BACK && jact < ActionType::SHELL_EVENT) {
                    BLOG(" ERROR: server action Parse pos length %d", (int) positionArray.size());
                }
            } else {
                BLOGE("no pos element in server action %s", optJsonStr.c_str());
            }
            this->throttle = static_cast<float>(getJsonValue<int>(retJson, "throttle", 0));
            this->adbInput = getJsonValue<bool>(retJson, "adb_input", false);
            this->waitTime = getJsonValue<int>(retJson, "wait_time", 0);
        }
        catch (nlohmann::json::exception &e) // may some char encoding error
        {
            BLOGE("Parse Operate For Device Error! %s", e.what());
        }
    }

    std::string DeviceOperateWrapper::toString() const {
        std::string ret;
        try {
            nlohmann::json retJson;
            retJson["act"] = actName[this->act];
            retJson["pos"] = nlohmann::json::array({this->pos.left, this->pos.top,
                                                    this->pos.right, this->pos.bottom});
            retJson["sid"] = this->sid;
            retJson["aid"] = this->aid;
            retJson["waitTime"] = this->waitTime;
            retJson["throttle"] = this->throttle;
            retJson["allowFuzzing"] = this->allowFuzzing;
            retJson["extra0"] = this->extra0;
            retJson["name"] = this->name;
            retJson["text"] = this->_text;
            retJson["clear"] = BOOL_TO_STR(this->clear);
            retJson["adbInput"] = BOOL_TO_STR(this->adbInput);
            retJson["rawInput"] = BOOL_TO_STR(this->rawInput);
            retJson["editable"] = BOOL_TO_STR(this->editable);
            retJson["jAction"] = this->jAction;
            ret = retJson.dump();
        }
        catch (nlohmann::json::exception &e) // may some char encoding error
        {
            BLOGE("Parse Operate For Device Error! %s", e.what());
            char returnCString[2000];
            sprintf(returnCString, "{\"act\":\"%s\", \"pos\":[%d,%d,%d,%d],\"sid\":\"%s\",        \
            \"aid\":\"%s\",\"waitTime\":%d,\"throttle\":%.2f,\"extra0\":\"%s\",    \
            \"name\":\"%s\",\"text\":\"%s\",\"allowFuzzing\":\"%s\",\"clear\":\"%s\",\"adbInput\":\"%s\",       \
            \"rawInput\":\"%s\",\"editable\":\"%s\",\"jAction\":\"%s\"}",
                    actName[this->act].c_str(),
                    pos.left, pos.top, pos.right, pos.bottom, sid.c_str(), aid.c_str(),
                    waitTime, throttle, extra0.c_str(), name.c_str(), _text.c_str(),
                    BOOL_TO_STR(this->allowFuzzing), BOOL_TO_STR(this->clear),
                    BOOL_TO_STR(this->adbInput),
                    BOOL_TO_STR(this->rawInput), BOOL_TO_STR(this->editable),
                    this->jAction.c_str());
            ret = std::string(returnCString);
        }
        return ret;
    }


    std::shared_ptr<DeviceOperateWrapper> DeviceOperateWrapper::OperateNop = std::make_shared<DeviceOperateWrapper>();

}

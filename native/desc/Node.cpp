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
#ifndef Node_CPP_
#define Node_CPP_

#include "Node.h"
#include "../utils.hpp"

namespace fastbotx {

    Node::Node()
            : _visitedCount(0), _id(0) {}

    void Node::visit(time_t timestamp) {
        _visitedCount++;
        BDLOG("visit id:%s times %d", this->getId().c_str(), this->_visitedCount);
    }

    std::string Node::toString() const {
        return this->getId();
    }

}
#endif //Node_CPP_

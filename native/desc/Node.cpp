/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
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

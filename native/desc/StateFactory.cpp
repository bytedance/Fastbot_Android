/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */

#include "StateFactory.h"
#include "State.h"
#include "reuse/ReuseState.h"

namespace fastbotx {

    StatePtr StateFactory::createState(AlgorithmType agentT, const stringPtr &activity,
                                       const ElementPtr &element) {
        StatePtr state = nullptr;
        state = ReuseState::create(element, activity);
        return state;
    }

}

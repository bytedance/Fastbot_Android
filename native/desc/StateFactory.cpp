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

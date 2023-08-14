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

#ifndef Agent_Factory_CPP_
#define Agent_Factory_CPP_

#include "AgentFactory.h"
#include "utils.hpp"
#include "Model.h"
#include "ModelReusableAgent.h"
#include "json.hpp"
#include "Preference.h"

namespace fastbotx {
/// No matter what kind of Algorithm you choose, only the ModelReusableAgent will be used.
/// \param agentT
/// \param model
/// \param deviceType
/// \return
    AbstractAgentPtr
    AgentFactory::create(AlgorithmType agentT, const ModelPtr &model, DeviceType deviceType) {
        AbstractAgentPtr agent = nullptr;
        // use ModelReusableAgent under all circumstances.
        ReuseAgentPtr reuseAgent = std::make_shared<ModelReusableAgent>(model);
        threadDelayExec(3000, false, &ModelReusableAgent::threadModelStorage,
                        std::weak_ptr<fastbotx::ModelReusableAgent>(reuseAgent));
        agent = reuseAgent;
        return agent;
    }

}

#endif

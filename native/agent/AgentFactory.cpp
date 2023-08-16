/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
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

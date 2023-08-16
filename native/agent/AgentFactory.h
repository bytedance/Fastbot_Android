/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */

#ifndef AgentFactory_H_
#define AgentFactory_H_

#include "AbstractAgent.h"
#include "Base.h"

namespace fastbotx {

    enum DeviceType {
        Normal
    };

    class Model;

    class AbstractAgent;

    typedef std::shared_ptr<Model> ModelPtr;
    typedef std::shared_ptr<AbstractAgent> AbstractAgentPtr;

    /// The factory class for creating different sorts of agents.
    class AgentFactory {
    public:

        ///
        /// \param agentT
        /// \param model
        /// \param deviceType
        /// \return
        static AbstractAgentPtr create(AlgorithmType agentT, const ModelPtr &model,
                                       DeviceType deviceType = DeviceType::Normal);
    };
}
#endif /* AgentFactory_H_ */

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
#ifndef ReuseAgent_H_
#define ReuseAgent_H_

#include "AbstractAgent.h"
#include "State.h"
#include "Action.h"
#include <vector>
#include <map>

namespace fastbotx {

#define SarsaRLDefaultAlpha   0.25
#define SarsaRLDefaultEpsilon 0.05
#define SarsaRLDefaultGamma   0.8

    typedef std::map<stringPtr, int> ReuseEntryM;
    typedef std::map<uint64_t, ReuseEntryM> ReuseEntryIntMap;
    typedef std::map<uint64_t, double> ReuseEntryQValueMap;

    class ModelReusableAgent : public AbstractAgent {

    public:
        explicit ModelReusableAgent(const ModelPtr &model);

        // load & save will be automatically called in construct & dealloc
        virtual void loadReuseModel(const std::string &packageName);

        // @param model filepath is "" then save to _defaultModelSavePath
        void saveReuseModel(const std::string &modelFilepath);

        static void threadModelStorage(const std::weak_ptr<ModelReusableAgent> &agent);

        ~ModelReusableAgent() override;

    protected:
        virtual double computeRewardOfLatestAction();

        void updateStrategy() override;

        virtual ActivityStateActionPtr selectNewActionEpsilonGreedyRandomly() const;

        virtual bool eGreedy() const;

        ActionPtr selectNewAction() override;

        double probabilityOfVisitingNewActivities(const ActivityStateActionPtr &action,
                                                  const stringPtrSet &visitedActivities) const;

        double getStateActionExpectationValue(const StatePtr &state,
                                              const stringPtrSet &visitedActivities) const;

        virtual void updateReuseModel();

        void adjustActions() override;

        ActionPtr selectUnperformedActionNotInReuseModel() const;

        /// Choose an unused(unvisited) action with quality value greater than zero
        /// under the influence of humble-gumbel distribution,
        /// \return The chosen action
        ActionPtr selectUnperformedActionInReuseModel() const;

        ActionPtr selectActionByQValue();


    protected:
        double _alpha{};
        double _epsilon{};

        // _rewardCache[i] is the reward value of _previousActions[i+1]
        std::vector<double> _rewardCache;
        std::vector<ActionPtr> _previousActions;

    private:
        // A map containing entry of hash code of Action and map, which containing entry of name of activity that this
        // action goes to and the count of this very activity being visited.
        ReuseEntryIntMap _reuseModel;
        ReuseEntryQValueMap _reuseQValue;
        std::string _modelSavePath;
        std::string _defaultModelSavePath;
        static std::string DefaultModelSavePath; // if the saved path is not specified, use this as the default.
        std::mutex _reuseModelLock;

        void computeAlphaValue();

        double getQValue(const ActionPtr &action);

        void setQValue(const ActionPtr &action, double qValue);
    };

    typedef std::shared_ptr<ModelReusableAgent> ReuseAgentPtr;

}


#endif /* ReuseAgent_H_ */

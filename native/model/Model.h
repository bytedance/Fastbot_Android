/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef  Model_H_
#define  Model_H_

#include <memory>
#include "Base.h"
#include "State.h"
#include "Element.h"
#include "Action.h"
#include "Graph.h"
#include "AbstractAgent.h"
#include "AgentFactory.h"
#include "Preference.h"

namespace fastbotx {

    class Model : public std::enable_shared_from_this<Model> {
    public:
        /// Create smart pointer of a new model object
        /// \return Smart pointer of a new model object
        static std::shared_ptr<Model> create();

        /// Get the size of states inside this graph
        /// \return Size of states inside this graph
        inline size_t stateSize() const { return this->getGraph()->stateSize(); }

        /// Get the graph GraphPtr object
        /// \return The graph object of GraphPtr type
        const GraphPtr &getGraph() const { return this->_graph; }

        /// @brief create and add agent to the agent map of the current model, and listen to the
        ///        changes from the graph, update states in time.
        /// @param deviceIDString did, or device id.
        /// @param agentType the type of agent being added
        /// @param deviceType
        /// @return the newly created agent
        AbstractAgentPtr addAgent(const std::string &deviceIDString, AlgorithmType agentType,
                                  DeviceType deviceType = DeviceType::Normal);

        /// Get the agent of the specified device.
        /// \param deviceID The device id string.
        /// \return The agent of the specified device.
        AbstractAgentPtr getAgent(const std::string &deviceID) const;

        /// The general entrance of getting next step according to RL model, and return the next operation step in json format
        /// \param descContent XML of the current page, in string format
        /// \param activity activity name
        /// \param deviceID The default value is "", you could provide your intended ID
        /// \return the next operation step in json format
        std::string getOperate(const std::string &descContent, const std::string &activity,
                               const std::string &deviceID = "");

        // get state from xml doc; for ios
        /// According to the constructed XML object of the current page, return the next operation step in json format with RL model
        /// \param element XML object of the current page, in XML format
        /// \param activity activity name string of the current page
        /// \param deviceID the device id of the current device
        /// \return the next operation step in json format
        std::string getOperate(const ElementPtr &element, const std::string &activity,
                               const std::string &deviceID = "");

        /// The core function for getting a new action and update the RL model
        /// \param element the XML object of the current page, in XML format
        /// \param activity the activity name string  of this current page
        /// \param deviceID the device id string of this current page
        /// \return an #DeviceOperateWrapper object containing the info for next operation
        OperatePtr getOperateOpt(const ElementPtr &element, const std::string &activity,
                                 const std::string &deviceID = "");

        PreferencePtr getPreference() const { return this->_preference; }

        void setPackageName(
                const std::string &packageName) { this->_netActionParam.packageName = packageName; }

        const std::string &getPackageName() const { return this->_netActionParam.packageName; }

        int getNetActionTaskID() const { return this->_netActionParam.netActionTaskid; }

        virtual ~Model();

    protected:
        Model();

    private:
        // The smart pointer of the graph object
        GraphPtr _graph;
        // A map containing pairs of device id and the corresponding agent object
        AbstractAgentPtrStrMap _deviceIDAgentMap;
        // Preference specified by users
        PreferencePtr _preference;

        // The parameters for communicating with the net model
        NetActionParam _netActionParam;

    };

    typedef std::shared_ptr<Model> ModelPtr;
}

#endif  // Model_H_

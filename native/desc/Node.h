/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Node_H_
#define Node_H_

#include <string>
#include <ctime>
#include "../Base.h"


#define PropertyIDPrefix(X)  \
        const static std::string __##X##Prefix

#define PropertyIDPrefixImpl(X, value) \
        const std::string X::__##X##Prefix = value

#define FuncGetID(X) \
        const std::string getId() const override {return X::__##X##Prefix+std::to_string(_id);}

namespace fastbotx {

// Used for statistic, it retains the status and counts of being visited.
    class Node : public Serializable {
    public:
        Node();

        /// Update the count of being visited
        /// \param timestamp The timestamp when it's been visited.
        virtual void visit(time_t timestamp);

        /// Test if this node has been visited or not
        /// \return true if it's been visited before
        bool isVisited() const { return _visitedCount > 0; }

        /// Get the visited count
        /// \return Visited count
        int getVisitedCount() const { return this->_visitedCount; }

        // implements Serializable
        std::string toString() const override;

        virtual const std::string getId() const { return std::to_string(_id); }

        void setId(const int &id) { this->_id = id; }

        int getIdi() const { return this->_id; }

    protected:
        int _visitedCount;
        int _id;
    };
}

#endif  // Node_H_

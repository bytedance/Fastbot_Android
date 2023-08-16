/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef SateFactory_H_
#define SateFactory_H_

#include "State.h"
#include "../Base.h"
#include "Element.h"

namespace fastbotx {

    class StateFactory {
    public:

        static StatePtr
        createState(AlgorithmType agentT, const stringPtr &activity, const ElementPtr &element);
    };
}
#endif /* SateFactory_H_ */

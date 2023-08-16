/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Widget_H_
#define Widget_H_


#include <set>
#include <map>
#include <string>
#include <memory>
#include "Element.h"
#include "../Base.h"

namespace fastbotx {

///Use text inside widget for embedding and identifying widget, which is the default implementation
    class Widget : Serializable, public HashNode {
    public:
        /// Use text inside widget for embedding and identifying widget, which is the default implementation.
        /// \param parent
        /// \param element
        Widget(std::shared_ptr<Widget> parent, const ElementPtr &element);

        std::shared_ptr<Widget> getParent() const { return this->_parent; }

        std::shared_ptr<Rect> getBounds() const { return this->_bounds; }

        std::set<ActionType> getActions() const { return this->_actions; }

        std::string getText() const { return this->_text; }

        bool getEnabled() const { return this->_enabled; }

        bool hasOperate(OperateType opt) const { return this->_operateMask & opt; }

        bool hasAction() const { return !this->_actions.empty(); }

        bool isEditable() const;

        uintptr_t hash() const override;

        std::string toString() const override;

        std::string buildFullXpath() const;

        virtual void clearDetails();

        void fillDetails(const std::shared_ptr<Widget> &copy);

        virtual ~Widget();


    protected:
        Widget();

        void enableOperate(OperateType opt) { this->_operateMask |= opt; }

        void initFormElement(const ElementPtr &element);

        uintptr_t _hashcode{};
        std::shared_ptr<Widget> _parent;
        std::string _text;
        int _index{};
        std::string _clazz;
        std::string _resourceID;
        bool _enabled{};
        bool _isEditable{};
        int _operateMask{OperateType::None};
    private:
        std::string toXPath() const;

        RectPtr _bounds;
        std::string _contextDesc;
        std::set<ActionType> _actions;
    };


    typedef std::shared_ptr<Widget> WidgetPtr;
    typedef std::vector<WidgetPtr> WidgetPtrVec;
    typedef std::set<WidgetPtr, Comparator<Widget>> WidgetPtrSet;
    typedef std::map<uintptr_t, WidgetPtrVec> WidgetPtrVecMap;

}


#endif //Widget_H_

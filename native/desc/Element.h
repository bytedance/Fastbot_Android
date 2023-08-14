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
#ifndef Element_H_
#define Element_H_

#include "../Base.h"
#include <string>
#include <utility>
#include <vector>
#include <memory>
#include <functional>

namespace tinyxml2 {
    class XMLElement;

    class XMLDocument;
}


namespace fastbotx {

    class Xpath {
    public:
        Xpath();

        explicit Xpath(const std::string &xpathString);

        std::string clazz;
        std::string resourceID;
        std::string text;
        std::string contentDescription;
        int index;
        bool operationAND;

        std::string toString() const { return _xpathStr; }

    private:
        std::string _xpathStr;
    };

    typedef std::shared_ptr<Xpath> XpathPtr;


///sdcard/max.feedmodel/structure
////sdcard/max.tree.pruning
// GUITreeNode

    class Element : public Serializable {
    public:
        Element();

        bool matchXpathSelector(const XpathPtr &xpathSelector) const;

        void deleteElement();


        bool isWebView() const;

        bool isEditText() const;

        const std::vector<std::shared_ptr<Element> > &
        getChildren() const { return this->_children; }

        // recursive get elements depends func
        void recursiveElements(const std::function<bool(std::shared_ptr<Element>)> &func,
                               std::vector<std::shared_ptr<Element>> &result) const;

        void recursiveDoElements(const std::function<void(std::shared_ptr<Element>)> &doFunc);

        std::weak_ptr<Element> getParent() const { return this->_parent; }

        const std::string &getClassname() const { return this->_classname; }

        const std::string &getResourceID() const { return this->_resourceID; }

        const std::string &getText() const { return this->_text; }

        const std::string &getContentDesc() const { return this->_contentDesc; }

        const std::string &getPackageName() const { return this->_packageName; }

        RectPtr getBounds() const { return this->_bounds; };

        int getIndex() const { return this->_index; }

        bool getClickable() const { return this->_clickable; }

        bool getLongClickable() const { return this->_longClickable; }

        bool getCheckable() const { return this->_checkable; }

        bool getScrollable() const { return this->_scrollable; }

        bool getEnable() const { return this->_enabled; }

        ScrollType getScrollType() const;

        // reset properties, in Preference
        void reSetResourceID(const std::string &resourceID) { this->_resourceID = resourceID; }

        void reSetContentDesc(const std::string &content) { this->_contentDesc = content; }

        void reSetText(const std::string &text) { this->_text = text; }

        void reSetIndex(const int &index) { this->_index = index; }

        void reSetClassname(const std::string &className) { this->_classname = className; }

        void reSetClickable(bool clickable) { this->_clickable = clickable; }

        void reSetScrollable(bool scrollable) { this->_scrollable = scrollable; }

        void reSetEnabled(bool enable) { this->_enabled = enable; }

        void reSetBounds(RectPtr rect) { this->_bounds = std::move(rect); }

        void reSetParent(const std::shared_ptr<Element> &parent) { this->_parent = parent; }

        void reAddChild(const std::shared_ptr<Element> &child) {
            this->_children.emplace_back(child);
        }

        std::string toJson() const;

        std::string toXML() const;

        void fromJson(const std::string &jsonData);

        std::string toString() const override;

        static std::shared_ptr<Element> createFromXml(const std::string &xmlContent);

        static std::shared_ptr<Element> createFromXml(const tinyxml2::XMLDocument &doc);

        long hash(bool recursive = true);

        std::string validText;

        virtual ~Element();

    protected:
        void fromXMLNode(const tinyxml2::XMLElement *xmlNode,
                         const std::shared_ptr<Element> &parentOfNode);

        void fromXml(const tinyxml2::XMLDocument &nodeOfDoc,
                     const std::shared_ptr<Element> &parentOfNode);

        void recursiveToXML(tinyxml2::XMLElement *xml, const Element *elm) const;

        std::string _resourceID;
        std::string _classname;
        std::string _packageName;
        std::string _text;
        std::string _contentDesc;
        std::string _inputText;
        std::string _activity;

        bool _enabled;
        bool _checked;
        bool _checkable;
        bool _clickable;
        bool _focusable;
        bool _scrollable;
        bool _longClickable;
        int _childCount;
        bool _focused;
        int _index;
        bool _password;
        bool _selected;
        bool _isEditable;

        RectPtr _bounds;
        std::vector<std::shared_ptr<Element> > _children;
        std::weak_ptr<Element> _parent;

        // a construct helper
        static bool _allClickableFalse;
    };

    typedef std::shared_ptr<Element> ElementPtr;


}

#endif //Element_H_

/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Element_CPP_
#define Element_CPP_

#include "../utils.hpp"
#include "Element.h"
#include "../thirdpart/tinyxml2/tinyxml2.h"
#include "../thirdpart/json/json.hpp"


namespace fastbotx {

    Element::Element()
            : _enabled(false), _checked(false), _checkable(false), _clickable(false),
              _focusable(false), _scrollable(false), _longClickable(false), _childCount(0),
              _focused(false), _index(0), _password(false), _selected(false), _isEditable(false) {
        _children.clear();
        this->_bounds = Rect::RectZero;
    }

    void Element::deleteElement() {
        auto parentOfElement = this->getParent();
        if (parentOfElement.expired()) {
            BLOGE("%s", "element is a root elements");
            return;
        }
        auto iter = std::remove_if(parentOfElement.lock()->_children.begin(),
                                   parentOfElement.lock()->_children.end(),
                                   [&](const ElementPtr &elem) { return elem.get() == this; }
        );
        if (iter != parentOfElement.lock()->_children.end()) {
            parentOfElement.lock()->_childCount--;
            parentOfElement.lock()->_children.erase(iter);
        }
        this->_parent.reset();
    }

/// According to given xpath selector, containing text, content, classname, resource id, test if
/// this current element has the same property value as the given xpath selector.
/// \param xpathSelector Describe property values of a xml element should have.
/// \return If this element could be matched to the given xpath selector, return true.
    bool Element::matchXpathSelector(const XpathPtr &xpathSelector) const {
        if (!xpathSelector)
            return false;
        bool match;
        bool isResourceIDEqual = (!xpathSelector->resourceID.empty() &&
                                  this->getResourceID() == xpathSelector->resourceID);
        bool isTextEqual = (!xpathSelector->text.empty() && this->getText() == xpathSelector->text);
        bool isContentEqual = (!xpathSelector->contentDescription.empty() &&
                               this->getContentDesc() == xpathSelector->contentDescription);
        bool isClassNameEqual = (!xpathSelector->clazz.empty() &&
                                 this->getClassname() == xpathSelector->clazz);
        bool isIndexEqual = xpathSelector->index > -1 && this->getIndex() == xpathSelector->index;
        BDLOG("begin find xpathSelector :\n "
              "XPathSelector:\n resourceID: %s text: %s contentDescription: %s clazz: %s index: %d \n"
              "UIPageElement:\n resourceID: %s text: %s contentDescription: %s clazz: %s index: %d \n"
              "equality: \n isResourceIDEqual:%d isTextEqual:%d isContentEqual:%d isClassNameEqual:%d isIndexEqual:%d",
              xpathSelector->resourceID.c_str(),
              xpathSelector->text.c_str(),
              xpathSelector->contentDescription.c_str(),
              xpathSelector->clazz.c_str(),
              xpathSelector->index,
              this->getResourceID().c_str(),
              this->getText().c_str(),
              this->getContentDesc().c_str(),
              this->getClassname().c_str(),
              this->getIndex(),
              isResourceIDEqual,
              isTextEqual,
              isContentEqual,
              isClassNameEqual,
              isIndexEqual);
        if (xpathSelector->operationAND) {
            match = true;
            if (!xpathSelector->clazz.empty())
                match = isClassNameEqual;
            if (!xpathSelector->contentDescription.empty())
                match = match && isContentEqual;
            if (!xpathSelector->text.empty())
                match = match && isTextEqual;
            if (!xpathSelector->resourceID.empty())
                match = match && isResourceIDEqual;
            if (xpathSelector->index != -1)
                match = match && isIndexEqual;
        } else
            match = isResourceIDEqual || isTextEqual || isContentEqual || isClassNameEqual;
        return match;
    }

/// Select elements if it satisfies bool function func
/// \param func bool function
/// \param result the vector for storing elements satisfying bool function func
    void Element::recursiveElements(const std::function<bool(ElementPtr)> &func,
                                    std::vector<ElementPtr> &result) const {
        if (func != nullptr) {
            for (const auto &child: this->_children) {
                if (func(child))
                    result.push_back(child);
                child->recursiveElements(func, result);
            }
        }
    }

    void Element::recursiveDoElements(const std::function<void(std::shared_ptr<Element>)> &doFunc) {
        if (doFunc != nullptr) {
            for (const auto &child: this->_children) {
                doFunc(child);
                child->recursiveDoElements(doFunc);
            }
        }
    }

    bool Element::_allClickableFalse = false;

    ElementPtr Element::createFromXml(const std::string &xmlContent) {
        tinyxml2::XMLDocument doc;
        std::vector<std::string> strings;
        int startIndex = 0, endIndex = 0;
        for (int i = 0; i <= xmlContent.size(); i++) {
            // If we reached the end of the word or the end of the input.
            if (xmlContent[i] == '\n' || i == xmlContent.size()) {
                endIndex = i;
                std::string temp;
                temp.append(xmlContent, startIndex, endIndex - startIndex);
                strings.push_back(temp);
                startIndex = endIndex + 1;
            }
        }
        for (auto it: strings) {
            BLOG("The content of XML is: %s", it.c_str());
        }
        tinyxml2::XMLError errXml = doc.Parse(xmlContent.c_str());

        if (errXml != tinyxml2::XML_SUCCESS) {
            BLOGE("parse xml error %d", (int) errXml);
            return nullptr;
        }

        ElementPtr elementPtr = std::make_shared<Element>();

        _allClickableFalse = true;
        elementPtr->fromXml(doc, elementPtr);
        if (_allClickableFalse) {
            elementPtr->recursiveDoElements([](const ElementPtr &elm) {
                elm->_clickable = true;
            });
        }
        // force set root element scrollable = true
        elementPtr->_scrollable = true;
        doc.Clear();
        return elementPtr;
    }

    ElementPtr Element::createFromXml(const tinyxml2::XMLDocument &doc) {
        ElementPtr elementPtr = std::make_shared<Element>(); // Use the empty element as the FAKE root element
        _allClickableFalse = true;
        elementPtr->fromXml(doc, elementPtr);
        if (_allClickableFalse) {
            elementPtr->recursiveDoElements([](const ElementPtr &elm) {
                elm->_clickable = true;
            });
        }
        return elementPtr;
    }

    void Element::fromJson(const std::string &jsonData) {
        //nlohmann::json
    }

    std::string Element::toString() const {
        return this->toJson();
    }


    std::string Element::toJson() const {
        // nlohmann::json json(this);
        return "Element ...... ";
    }

    void Element::recursiveToXML(tinyxml2::XMLElement *xml, const Element *elm) const {
        BDLOG("add a xml %p %p", xml, elm);
        xml->SetAttribute("bounds", elm->getBounds()->toString().c_str());
        BDLOG("add a xml 111");
        xml->SetAttribute("index", elm->getIndex());
        xml->SetAttribute("class", elm->getClassname().c_str());
        xml->SetAttribute("resource-id", elm->getResourceID().c_str());
        xml->SetAttribute("package", elm->getPackageName().c_str());
        xml->SetAttribute("content-desc", elm->getContentDesc().c_str());
        xml->SetAttribute("checkable", elm->getCheckable() ? "true" : "false");
        xml->SetAttribute("checked", elm->_checked ? "true" : "false");
        xml->SetAttribute("clickable", elm->getClickable() ? "true" : "false");
        xml->SetAttribute("enabled", elm->getEnable() ? "true" : "false");
        xml->SetAttribute("focusable", elm->_focusable ? "true" : "false");
        xml->SetAttribute("focused", elm->_focused ? "true" : "false");
        xml->SetAttribute("scrollable", elm->_scrollable ? "true" : "false");
        xml->SetAttribute("long-clickable", elm->_longClickable ? "true" : "false");
        xml->SetAttribute("password", elm->_password ? "true" : "false");
        xml->SetAttribute("scroll-type", "none");

        BDLOG("add a xml 1111");
        for (const auto &child: elm->getChildren()) {
            tinyxml2::XMLElement *xmlChild = xml->InsertNewChildElement("node");
            xml->LinkEndChild(xmlChild);
            recursiveToXML(xmlChild, child.get());
        }
    }

    std::string Element::toXML() const {
        tinyxml2::XMLDocument doc;
        tinyxml2::XMLDeclaration *xmlDeclarationNode = doc.NewDeclaration();
        doc.InsertFirstChild(xmlDeclarationNode);

        tinyxml2::XMLElement *root = doc.NewElement("node");
        recursiveToXML(root, this);
        doc.LinkEndChild(root);

        tinyxml2::XMLPrinter printer;
        doc.Print(&printer);
        std::string xmlStr = std::string(printer.CStr());
        return xmlStr;
    }

    void Element::fromXml(const tinyxml2::XMLDocument &nodeOfDoc, const ElementPtr &parentOfNode) {
        const ::tinyxml2::XMLElement *node = nodeOfDoc.RootElement();
        this->fromXMLNode(node, parentOfNode);

        if (0 != nodeOfDoc.ErrorID())
            BLOGE("parse xml error %s", nodeOfDoc.ErrorStr());

    }

    void Element::fromXMLNode(const tinyxml2::XMLElement *xmlNode, const ElementPtr &parentOfNode) {
        if (nullptr == xmlNode)
            return;
//    BLOG("This Node is %s", std::string(xmlNode->GetText()).c_str());
        int indexOfNode = 0;
        tinyxml2::XMLError err = xmlNode->QueryIntAttribute("index", &indexOfNode);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_index = indexOfNode;
        }
        const char *boundingBoxStr = "get attribute failed";
        err = xmlNode->QueryStringAttribute("bounds", &boundingBoxStr);
        if (err == tinyxml2::XML_SUCCESS) {
            int xl, yl, xr, yr;
            if (sscanf(boundingBoxStr, "[%d,%d][%d,%d]", &xl, &yl, &xr, &yr) == 4) {
                this->_bounds = std::make_shared<Rect>(xl, yl, xr, yr);
                if (this->_bounds->isEmpty())
                    this->_bounds = Rect::RectZero;
            }
        }
        const char *text = "attribute text get failed";  // need copy
        err = xmlNode->QueryStringAttribute("text", &text);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_text = std::string(text); // copy
        }
        const char *resource_id = "attribute resource_id get failed";  // need copy
        err = xmlNode->QueryStringAttribute("resource-id", &resource_id);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_resourceID = std::string(resource_id); // copy
        }
        const char *tclassname = "attribute class name get failed";  // need copy
        err = xmlNode->QueryStringAttribute("class", &tclassname);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_classname = std::string(tclassname); // copy
        }
        const char *pkgname = "attribute package name get failed";  // need copy
        err = xmlNode->QueryStringAttribute("package", &pkgname);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_packageName = std::string(pkgname); // copy
        }
        const char *content_desc = "attribute content description get failed";  // need copy
        err = xmlNode->QueryStringAttribute("content-desc", &content_desc);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_contentDesc = std::string(content_desc); // copy
        }
        bool checkable = false;
        err = xmlNode->QueryBoolAttribute("checkable", &checkable);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_checkable = checkable;
        }
        bool clickable = false;
        err = xmlNode->QueryBoolAttribute("clickable", &clickable);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_clickable = clickable;
        }
        if (clickable)
            _allClickableFalse = false;
        bool checked = false;
        err = xmlNode->QueryBoolAttribute("checked", &checked);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_checked = checked;
        }
        bool enabled = false;
        err = xmlNode->QueryBoolAttribute("enabled", &enabled);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_enabled = enabled;
        }
        bool focused = false;
        err = xmlNode->QueryBoolAttribute("focused", &focused);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_focused = focused;
        }
        bool focusable = false;
        err = xmlNode->QueryBoolAttribute("focusable", &focusable);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_focusable = focusable;
        }
        bool scrollable = false;
        err = xmlNode->QueryBoolAttribute("scrollable", &scrollable);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_scrollable = scrollable;
        }
        bool longclickable = false;
        err = xmlNode->QueryBoolAttribute("long-clickable", &longclickable);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_longClickable = longclickable;
        }
        bool password = false;
        err = xmlNode->QueryBoolAttribute("password", &password);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_password = password;
        }
        bool selected = false;
        err = xmlNode->QueryBoolAttribute("selected", &selected);
        if (err == tinyxml2::XML_SUCCESS) {
            this->_selected = selected;
        }

        this->_isEditable = "android.widget.EditText" == this->_classname;
        if (FORCE_EDITTEXT_CLICK_TRUE && this->_isEditable) {
            this->_longClickable = this->_clickable = this->_enabled = true;
        }

        if (PARENT_CLICK_CHANGE_CHILDREN && parentOfNode && parentOfNode->_longClickable) {
            this->_longClickable = parentOfNode->_longClickable;
        }
        if (PARENT_CLICK_CHANGE_CHILDREN && parentOfNode && parentOfNode->_clickable) {
            this->_clickable = parentOfNode->_clickable;
        }
        if (this->_clickable || this->_longClickable) {
            this->_enabled = true;
        }

        int childrenCountOfCurrentNode = 0;
        if (!xmlNode->NoChildren()) {
            for (const tinyxml2::XMLElement *childNode = xmlNode->FirstChildElement();
                 nullptr != childNode; childNode = childNode->NextSiblingElement()) {
                const tinyxml2::XMLElement *nextXMLElement = childNode;
                ElementPtr childElement = std::make_shared<Element>();
                this->_children.emplace_back(childElement);
                childrenCountOfCurrentNode++;
                // generate XML for deeper children, pass the current xmlNode as their parent
                childElement->fromXMLNode(nextXMLElement, childElement);
                // update the parent of this current child
                childElement->_parent = parentOfNode;
            }
        }
        this->_childCount = childrenCountOfCurrentNode;
    }

    bool Element::isWebView() const {
        return "android.webkit.WebView" == this->_classname;
    }

    bool Element::isEditText() const {
        return this->_isEditable;
    }

    ScrollType Element::getScrollType() const {
        if (!this->_scrollable) {
            return ScrollType::NONE;
        }
        if ("android.widget.ScrollView" == this->_classname
            || "android.widget.ListView" == _classname
            || "android.widget.ExpandableListView" == _classname
            || "android.support.v17.leanback.widget.VerticalGridView" == _classname
            || "android.support.v7.widget.RecyclerView" == _classname
            || "androidx.recyclerview.widget.RecyclerView" == _classname) {
            return ScrollType::Vertical;
        } else if ("android.widget.HorizontalScrollView" == _classname
                   || "android.support.v17.leanback.widget.HorizontalGridView" == _classname
                   || "android.support.v4.view.ViewPager" == _classname) {
            return ScrollType::Horizontal;
        }
        if (this->_classname.find("ScrollView") != std::string::npos) {
            return ScrollType::ALL;
        }

        // for ios
//    return ScrollType::NONE;
        return ScrollType::ALL;
    }

    Element::~Element() {
        this->_children.clear();
        this->_parent.reset();
    }

    long Element::hash(bool recursive) {
        uintptr_t hashcode = 0x1;
        uintptr_t hashcode1 = 127U * std::hash<std::string>{}(this->_resourceID) << 1;
        uintptr_t hashcode2 = std::hash<std::string>{}(this->_classname) << 2;
        uintptr_t hashcode3 = std::hash<std::string>{}(this->_packageName) << 3;
        uintptr_t hashcode4 = 256U * std::hash<std::string>{}(this->_text) << 4;
        uintptr_t hashcode5 = std::hash<std::string>{}(this->_contentDesc) << 5;
        uintptr_t hashcode6 = std::hash<std::string>{}(this->_activity) << 2;
        uintptr_t hashcode7 = 64U * std::hash<int>{}(this->_clickable) << 6;

        hashcode =
                hashcode1 ^ hashcode2 ^ hashcode3 ^ hashcode4 ^ hashcode5 ^ hashcode6 ^ hashcode7;
        if (recursive) {
            for (int i = 0; i < this->_children.size(); i++) {
                long childHash = this->_children[i]->hash() << 2;
                hashcode ^= childHash;
                // with order
                hashcode ^= 0x7398c + (std::hash<int>{}(i) << 8);
            }
        }
        return static_cast<long >(hashcode);
    }

}

#endif //Element_CPP_

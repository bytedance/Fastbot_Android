/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su, Zhao Zhang
 */
#ifndef Preference_H_
#define Preference_H_

#include <string>
#include <map>
#include <vector>
#include <queue>
#include "Base.h"
#include "Action.h"
#include "DeviceOperateWrapper.h"
#include "Element.h"


namespace fastbotx {

    /// The class for describing the actions that user specified in preference file
    class CustomAction : public Action {
    public:
        OperatePtr toOperate() const override;

        CustomAction();

        explicit CustomAction(ActionType act);

        XpathPtr xpath;
        std::string resourceID;
        std::string contentDescription;
        std::string text;
        std::string classname;
        std::string activity;
        std::string command;
        std::vector<float> bounds;
        bool allowFuzzing{true};
        bool clearText{};
        int throttle{};
        int waitTime{};
        bool adbInput{};

        ~CustomAction() override = default;
    };

    typedef std::shared_ptr<CustomAction> CustomActionPtr;
    typedef std::vector<CustomActionPtr> CustomActionPtrVec;

    class CustomEvent {
    public:
        float prob;
        int times;
        std::string activity;

        CustomActionPtrVec actions;
    };

    typedef std::shared_ptr<CustomEvent> CustomEventPtr;
    typedef std::vector<CustomEventPtr> CustomEventPtrVec;
    typedef std::map<std::string, std::vector<RectPtr>> StringRectsMap;

    class Preference {
    public:
        Preference();

        static std::shared_ptr<Preference> inst();

        //@brief use custom preference correct the root xml, and return a custom action,
        //@return nullptr if no custom action happened
        ActionPtr
        resolvePageAndGetSpecifiedAction(const std::string &activity, const ElementPtr &rootXML);

        //@brief patch operate: 1. fuzz input text 2. ..
        void patchOperate(const OperatePtr &opt);

        // load resource mapping file, override the mapings from default file max.mapping,
        void loadMixResMapping(const std::string &resourceMappingPath);

        // load label, text, button valid text dumped from apk
        void loadValidTexts(const std::string &pathOfValidTexts);

        bool checkPointIsInBlackRects(const std::string &activity, int pointX, int pointY);

        void setListenMode(bool listen);

        bool skipAllActionsFromModel() const { return this->_skipAllActionsFromModel; }

        bool isForceUseTextModel() const { return this->_forceUseTextModel; }

        int getForceMaxBlockStateTimes() const { return this->_forceMaxBlockStateTimes; }

        ~Preference();

    protected:

        ///after the activity matches, resolve the black widgets, tree pruning, valid texts
        void resolvePage(const std::string &activity, const ElementPtr &rootXML);

        void deMixResMapping(const ElementPtr &rootXML);

        bool patchActionBounds(const CustomActionPtr &action, const ElementPtr &);

        // recursive resolve the elems, than resolve the black widgets, tree pruning, valid texts
        void resolveElement(const ElementPtr &element, const std::string &activity);

        // recursive
        void resolveBlackWidgets(const ElementPtr &rootXML, const std::string &activity);

        //  not recursive
        void resolveTreePruning(const ElementPtr &elem, const std::string &activity);

        // not recursive
        void pruningValidTexts(const ElementPtr &element);

        // recursive
        void
        findMatchedElements(std::vector<ElementPtr> &outElements, const XpathPtr &xpathSelector,
                            const ElementPtr &elementXML);

        void cachePageTexts(const ElementPtr &rootElement);

        void loadConfigs();

        void loadBaseConfig();

        void loadActions();

        void loadBlackWidgets();

        void loadWhiteBlackList();

        void loadInputTexts();

        void loadTreePruning();

    private:

        static std::shared_ptr<Preference> _preferenceInst;

        std::queue<ActionPtr> _currentActions;

        CustomEventPtrVec _customEvents;
        // remember the times of this event being visited.
        std::map<CustomEventPtr, int> _eventTimes;

        std::vector<std::string> _whiteList;
        std::vector<std::string> _blackList;

        std::vector<std::string> _inputTexts;
        std::vector<std::string> _fuzzingTexts;
        std::vector<std::string> _pageTextsCache;

        CustomActionPtrVec _blackWidgetActions;
        CustomActionPtrVec _treePrunings;

        std::map<std::string, std::string> _resMapping;
        std::map<std::string, std::string> _resMixedMapping;

        bool _randomInputText;
        bool _doInputFuzzing;

        std::set<std::string> _validTexts;
        bool _pruningValidTexts;
        bool _skipAllActionsFromModel;
        bool _forceUseTextModel{};
        int _forceMaxBlockStateTimes{};
        RectPtr _rootScreenSize;

        static std::string loadFileContent(const std::string &fileAbsolutePath);

        StringRectsMap _cachedBlackWidgetRects;

    public:
        static std::string InvalidProperty;
        // static configs for android
        static std::string DefaultResMappingFilePath;
        static std::string BaseConfigFilePath;      // /sdcard/max.config
        static std::string InputTextConfigFilePath; // /sdcard/max.strings
        static std::string ActionConfigFilePath;    // /sdcard/max.xpath.actions
        static std::string WhiteListFilePath;       // /sdcard/awl.strings
        static std::string BlackListFilePath;       // /sdcard/abl.strings
        static std::string BlackWidgetFilePath;     // /sdcard/max.widget.black
        static std::string TreePruningFilePath;     // /sdcard/max.tree.pruning
        static std::string ValidTextFilePath;       // /sdcard/max.valid.strings
        static std::string FuzzingTextsFilePath;    // /sdcard/max.fuzzing.strings
        static std::string PackageName;
    };

    typedef std::shared_ptr<Preference> PreferencePtr;

};

#endif //Preference_H_

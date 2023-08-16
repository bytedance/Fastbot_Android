/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su
 */
#ifndef BASE_H_
#define BASE_H_

#include <string>
#include <vector>
#include <set>
#include <memory>
#include <atomic>
#include <cstdlib>
#include <sstream>
#include <ctime>
#include <sys/time.h>
#include <thread>
#include <functional>
#include <chrono>
#include <cmath>

#include "json.hpp"

#ifdef __ANDROID__

#include <jni.h>

namespace std {
    template<typename T>
    string to_string(const T &n) {
        ostringstream stm;
        stm << n;
        return stm.str();
    }
}
#endif //__ANDROID__

namespace fastbotx {


    class PriorityNode {
    public:
        PriorityNode();

        virtual int getPriority() const { return this->_priority; };


        virtual bool operator<(const PriorityNode &node) const {
            return this->getPriority() < node.getPriority();
        }

    protected:
        int _priority;
    private:
    };

    class Serializable {
    public:
        virtual std::string toString() const = 0;
    };

    /// Class for identifying node or element, could be used for merging elements.
    class HashNode {
    public:
        virtual uintptr_t hash() const {
            return reinterpret_cast<uintptr_t>(this);
        }

        /// for std:: set/ map/
        /// \param right
        /// \return
        virtual inline bool operator==(const HashNode &right) {
            return this->hash() == right.hash();
        }

        // for std::sort
        virtual inline bool operator<(const HashNode &right) {
            return this->hash() < right.hash();
        }
    };


    template<typename T>
    bool equals(const std::shared_ptr<T> &left, const std::shared_ptr<T> &right) {
        if (nullptr == left || nullptr == right) {
            return false;
        }
        return *(left.get()) == *(right.get());
    }

    // for std::sort
    template<typename T>
    struct Comparator : public std::binary_function<std::shared_ptr<T>, std::shared_ptr<T>, bool> {
        bool operator()(std::shared_ptr<T> const &left, std::shared_ptr<T> const &right) const {
            return *left.get() < *right.get();
        }
    };


    typedef std::shared_ptr<std::string> stringPtr;
    typedef std::set<stringPtr, Comparator<std::string>> stringPtrSet;

    /// Compute the hash code according to the given vector, and embed order index as well when possible.
    /// \tparam T The class type for hashing
    /// \param vector The vector of shared pointer of class T
    /// \param withOrder If true, then this method will encode the order of entry into the final hash code as well
    /// \return The final hash code
    template<typename T>
    uintptr_t combineHash(const std::vector<std::shared_ptr<T> > &vector, bool withOrder) {
        size_t count = vector.size();
        uintptr_t combinedHashcode = 0x1;
        for (size_t i = 0; i < count; i++) {
            auto hashNode = std::dynamic_pointer_cast<HashNode>(vector.at(i));
            if (hashNode != nullptr) {
                combinedHashcode ^= (hashNode->hash());
                if (withOrder)
                    combinedHashcode ^= (127U * (i << 6));
            }
        }
        return combinedHashcode;
    }

    /// Compute the hash code according to the given vector, and embed order index as well when possible.
    /// \tparam Iter The class type for hashing
    /// \param it Need to pass in a type of iterator, which is the beginning of the iterator
    /// \param end The end of the iterator
    /// \param withOrder If true, then this method will encode the order of entry into the final hash code as well
    /// \return The final hash code
    template<typename Iter>
    uintptr_t combineHash(const Iter it, const Iter end, bool withOrder) {
        uintptr_t hashCode = 0x1;
        int indexOfElement = 0;
        Iter cursor = it;
        for (; cursor != end; ++cursor) {
            auto inode = std::dynamic_pointer_cast<HashNode>((*cursor));
            if (inode != nullptr) {
                hashCode ^= inode->hash();
                if (withOrder)
                    hashCode ^= (127U * (indexOfElement << 6));
            }
            indexOfElement++;
        }
        return hashCode;
    }

    // ActionType
    enum ActionType {
        CRASH = 0,

        FUZZ,

        START,
        RESTART,
        CLEAN_RESTART,
        NOP,
        ACTIVATE,

        BACK,
        FEED,
        CLICK,
        LONG_CLICK,
        SCROLL_TOP_DOWN,
        SCROLL_BOTTOM_UP,
        SCROLL_LEFT_RIGHT,
        SCROLL_RIGHT_LEFT,
        SCROLL_BOTTOM_UP_N,
        SHELL_EVENT,
        Hover,
        ActTypeSize // 18
    };
    extern const std::string actName[];

    ActionType stringToActionType(const std::string &actionTypeString);

    enum ScrollType {
        ALL = 0,
        Horizontal,
        Vertical,
        NONE,
        VerticalSeries,
        ScrollTypeSize // 5
    };
    extern const std::string ScrollTypeName[];

    ScrollType stringToScrollType(const std::string &str);

    enum TransitionVisitType {
        NEW_ACTION, // new edge for this action
        NEW_ACTION_TARGET, // new edge for this action and target
        EXISTING // existing edge for this action and target
    };


    enum OperateType {
        None = 0,
        Enable = 0x0001,
        Clickable = Enable << 1,
        Checkable = Enable << 2,
        LongClickable = Enable << 3,
        Scrollable = Enable << 4,
        Inputable = Enable << 5
    };


    enum AlgorithmType {
        Random = 0,
        Reuse = 4,
        Server = 6
    };


    class Point : public HashNode {
    public:
        int x;
        int y;

    public:
        Point();

        Point(const Point &point);

        Point(int x, int y);

        uintptr_t hash() const override;

        bool operator==(const Point &node) const;

        Point &operator=(const Point &node);
    };

    class Rect : HashNode {
    public:
        int top;
        int bottom;
        int left;
        int right;

    public:
        Rect();

        Rect(const Rect &rect);

        Rect(int left, int top, int right, int bottom);

        virtual ~Rect() = default;

        bool isEmpty() const;

        bool contains(const Point &point) const;

        Point center() const;

        uintptr_t hash() const override;

        std::string toString() const;

        bool operator==(const Rect &node) const;

        Rect &operator=(const Rect &node);
        // Rect & operator=(const Rect&& rect);

        // a  reference, means do not change it's value
        static std::shared_ptr<Rect> getRect(const std::shared_ptr<Rect> &rect);

        static const std::shared_ptr<Rect> RectZero;
    private:
        static std::vector<std::shared_ptr<Rect>> _rectPool;
    };

    typedef std::shared_ptr<Rect> RectPtr;


    inline bool isZhCn(const char &c) {
        if (~(c >> 8) == 0) {
            return true;
        }
        return false;
    }

    inline int randomInt(int min, int max) {
        // std::srand((unsigned int)std::time(nullptr));
        int rand = std::rand();
        return min + rand % (max - min);
    }


    inline int randomInt(int min, int max, int seed) {
        std::srand((unsigned int) std::time(nullptr) + seed * 2989);
        int rand = std::rand();
        return min + rand % (max - min);
    }

    inline void trimString(std::string &str) {
        str.erase(0, str.find_first_not_of(' '));
        str.erase(str.find_last_not_of(' ') + 1);
    }

    inline void
    splitString(const std::string &str, std::vector<std::string> &strVec, char splitChar) {
        std::stringstream ss(str);
        std::string iter;
        while (std::getline(ss, iter, splitChar)) {
            strVec.push_back(iter);
        }
    }


    template<typename T>
    inline T getJsonValue(::nlohmann::json elem, const char *key, T defaultvalue) {
        T t = defaultvalue;
        if (elem.contains(key) && !(elem[key].is_null()))
            t = elem[key].get<T>();
        return t;
    }

    inline std::string
    stringReplaceAll(std::string &str, const std::string &from, const std::string &to) {
        size_t start_pos = 0;
        while ((start_pos = str.find(from, start_pos)) != std::string::npos) {
            str.replace(start_pos, from.length(), to);
            start_pos += to.length();
        }
        return str;
    }

    inline std::string getTimeFormatStr() {
        time_t now = time(nullptr);
        struct tm timeStruct{};
        char buf[80];
        timeStruct = *localtime(&now);
        // Visit http://en.cppreference.com/w/cpp/chrono/c/strftime
        // for more information about date/time format
        strftime(buf, sizeof(buf), "%Y-%m-%d %T", &timeStruct);
        return {buf};
    }

    inline double currentStamp() {
        struct timeval timeValue{};
        gettimeofday(&timeValue, nullptr);
        return (timeValue.tv_sec) * 1000 + (timeValue.tv_usec) / 1000.0;
    }

    template<typename callable, typename... arguments>
    inline bool threadDelayExec(int delayMS, bool async, callable handler, arguments... args) {
        std::function<typename std::result_of<callable(arguments...)>::type()> task(
                std::bind(handler, args...));

        std::thread t = std::thread([task, delayMS]() {
            std::this_thread::sleep_for(std::chrono::milliseconds(delayMS));
            task();
        });
        if (async) {
            t.join();
        } else {
            t.detach();
        }
        return true;
    }

#define AlphabetSeqLen 88
#define AlphabetChSeqLen 64
    static const char AlphabetSeq[AlphabetSeqLen] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~!@#$%^&*()<>_-;',.?/\"|{}";// len 84
    static const char AlphabetChSeq[AlphabetChSeqLen] = "你好中文字符串｜。，；（）【】？！测试啊哈"; // len 64

    inline std::string getRandomChars() {
        std::stringstream randomStringStream;
        int len = randomInt(11, 1000);
        auto seed = (unsigned int) std::time(nullptr);
        while (len-- > 0) {
            std::srand(len * 2 ^ 8 + seed);
            int rand = std::rand();
            int i = rand % (AlphabetSeqLen * 4 + AlphabetChSeqLen);
            if (i < AlphabetSeqLen * 4) {
                i /= 4;
                randomStringStream << AlphabetSeq[i];
            } else {
                i -= (AlphabetSeqLen * 4);
                i = i / 3 * 3;
                randomStringStream << AlphabetChSeq[i] << AlphabetChSeq[i + 1]
                                   << AlphabetChSeq[i + 2];
                len -= 2;
            }
        }
        return randomStringStream.str();
    }

}
#endif //BASE_H_

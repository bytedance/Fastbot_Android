/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su
 */

#define _DEBUG_ 1
#define TAG "[Fastbot]"

#ifdef __ANDROID__

#include <android/log.h>

#define LOGD(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,fmt, ##__VA_ARGS__)
#define LOGI(fmt, ...) __android_log_print(ANDROID_LOG_INFO,TAG ,fmt, ##__VA_ARGS__)
#define LOGW(fmt, ...) __android_log_print(ANDROID_LOG_WARN,TAG ,fmt, ##__VA_ARGS__)
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR,TAG ,fmt, ##__VA_ARGS__)
#define LOGF(fmt, ...) __android_log_print(ANDROID_LOG_FATAL,TAG ,fmt, ##__VA_ARGS__)
#else
#define Time_Format_Now (getTimeFormatStr().c_str())
#define LOGD(fmt, ...) printf(TAG "[%s] DEBUG[%s][%s][%d]:" fmt "\n", Time_Format_Now, __FILE__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define LOGI(fmt, ...) printf(TAG "[%s] :" fmt "\n", Time_Format_Now ,##__VA_ARGS__)
#define LOGW(fmt, ...) printf(TAG "[%s] WARNING:" fmt "\n", Time_Format_Now, ##__VA_ARGS__)
#define LOGE(fmt, ...) printf(TAG "[%s] ERROR:" fmt "\n", Time_Format_Now, ##__VA_ARGS__)
#define LOGF(...)
#endif

#ifdef __ANDROID__
#define ACTIVITY_VC_STR "activity"
#else
#define ACTIVITY_VC_STR "ViewController"
#endif

//#if _DEBUG_
#define BDLOG(fmt, ...)   LOGD(fmt,##__VA_ARGS__)
#define BDLOGE(fmt, ...)  LOGE(fmt,##__VA_ARGS__)
//#else
//#define BDLOGE(...)
//#define BDLOG(...)
//#endif

#define BLOG(fmt, ...)    LOGI(fmt,##__VA_ARGS__)
#define BLOGE(fmt, ...)   LOGE(fmt,##__VA_ARGS__)

// If should drop detail after hashing
#define DROP_DETAIL_AFTER_SATE 1

// If should generate hash based on text
#define STATE_WITH_TEXT        0

// Whether the text attribute participates in the hash generation,
// the longest length of the text participating in the abstraction,
// generally a multiple of 3. More than 2 Chinese characters will
// not participate in the abstraction, the length of the character will be truncated,
#define STATE_TEXT_MAX_LEN     (2*3)

// If should generate hash based on index
#define STATE_WITH_INDEX       0

// If should order widgets before generating hash
#define STATE_WITH_WIDGET_ORDER 0

#define STATE_MERGE_DETAIL_TEXT 1

#define BLOCK_STATE_TIME_RESTART (-1)

#define FORCE_EDITTEXT_CLICK_TRUE 1

#define PARENT_CLICK_CHANGE_CHILDREN 1

#define SCROLL_BOTTOM_UP_N_ENABLE 0

#define FASTBOT_VERSION "local build"



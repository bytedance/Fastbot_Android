# @author Jianqiang Guo, Yuhui Su
cmake_minimum_required(VERSION 3.10)
project(fastbot_native)

set( LIBPATH mac)
message(STATUS  ${CMAKE_SYSTEM_NAME})
IF (CMAKE_SYSTEM_NAME MATCHES "Android")
  message(STATUS "${ANDROID_NDK}/sources/third_party/opencv/jni")
  set( OpenCV_DIR "${ANDROID_NDK}/sources/third_party/opencv/jni" )
  add_definitions(-D__ANDROID__ )
  set( LIBPATH android/libs/${ANDROID_ABI}/)
ELSEIF (CMAKE_SYSTEM_NAME MATCHES "Darwin")
   set( LIBPATH mac)
ELSEIF (CMAKE_SYSTEM_NAME MATCHES "Linux")
  set( LIBPATH linux)
ENDIF (CMAKE_SYSTEM_NAME MATCHES "Android")

IF (CMAKE_BUILD_TYPE MATCHES "Release")
 message(STATUES "compile release")
ELSE (CMAKE_BUILD_TYPE MATCHES "Release")
add_definitions(-D_DEBUG_)
ENDIF (CMAKE_BUILD_TYPE MATCHES "Release")

set(ANDROID_STL "c++_static")
set(CMAKE_CXX_STANDARD 14) 
set(CMAKE_CXX_STANDARD_REQUIRED on)
set(LOCAL_CXX_FLAGS "-std=c++14 -fPIC  -fvisibility=hidden -std=c++11 -frtti -Wno-switch-enum -Wno-switch ${LOCAL_CPPFLAGS}")

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../libs/${ANDROID_ABI}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../libs/${ANDROID_ABI}")

set(ANDROID_ARM_NEON ON)

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE}  -s -Wall ")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE}  -s -Wall ")

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

include_directories(
  "./"
  "agent/"
  "desc/"
  "desc/reuse"
  "events/"
  "model/"
  "monkey/"
  "project/jni/"
  "storage"
  "thirdpart/"
  "thirdpart/flatbuffers/"
  "thirdpart/tinyxml2/"
  "thirdpart/json/"
)

file( GLOB SRC_LIST 
      "*.hpp" 
      "*.cpp" 
      "desc/*.cpp" 
      "desc/reuse/*.cpp" 
      "model/*.cpp" 
      "agent/*.cpp"
      "events/*.cpp"
      "monkey/*.cpp" 
      "thirdpart/tinyxml2/*.cpp"
      "thirdpart/flatbuffers/*.cpp"
      "thirdpart/json/*.hpp")

message(STATUS ${SRC_LIST})

add_library( # Sets the name of the library.
             fastbot_native
             # Sets the library as a shared library.
             SHARED
             # Provides a relative path to your source file(s).
             ${SRC_LIST}
             "project/jni/fastbot_native.cpp"
        )

find_package(Threads)

find_program(CCACHE_FOUND ccache)
if(CCACHE_FOUND)
  message("Found ccache ${CCACHE_FOUND}")
  message("Using ccache to speed up compilation")
  set(ENV{CCACHE_CPP2} "yes")
  set_property(GLOBAL PROPERTY RULE_LAUNCH_COMPILE ccache)
  set_property(GLOBAL PROPERTY RULE_LAUNCH_LINK ccache)
endif(CCACHE_FOUND)

if (NOT CMAKE_BUILD_TYPE)
  message(STATUS "No build type selected, default to Release")
  set(CMAKE_BUILD_TYPE "Release")
endif()

target_link_libraries(
               fastbot_native
               log
               android
               atomic
            )

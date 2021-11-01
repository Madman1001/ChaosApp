# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

set( LIB_FMOD ${API_ROOT}/libs/${ANDROID_ABI}/libfmod.so )

add_library( fmod STATIC IMPORTED )

set_target_properties(
        fmod PROPERTIES
        IMPORTED_LOCATION ${LIB_FMOD}
        INTERFACE_INCLUDE_DIRECTORIES ${API_ROOT}/src/main/inc
)



add_library(
        example
        SHARED
        src/main/cpp/common.cpp
        src/main/cpp/common_platform.cpp
        src/main/cpp/play_sound.cpp
)

target_link_libraries( example fmod )

# build executable
# add_executable()

# Specifies a path to native header files.
#include_directories(C:/Users/liaohaorui/AppData/Local/Android/Sdk/ndk/21.1.6352462/sources/cxx-stl/llvm-libc++/include)
#include_directories(C:/Users/liaohaorui/AppData/Local/Android/Sdk/ndk/21.1.6352462/sources/android/support/include)
#include_directories(C:/Users/liaohaorui/AppData/Local/Android/Sdk/ndk/21.1.6352462/sources/android/ndk_helper)

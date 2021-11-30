# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             chaos_vpn

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             vpn_data.h vpn_log.c vpn_ip_data_utils.c vpn_ip_object.c vpn_java_utils.c vpn_jni_interface.c vpn_udp_data_utils.c vpn_udp_object.c)

find_library(log-lib log)

target_link_libraries(chaos_vpn ${log-lib})

# build executable
# add_executable()

# Specifies a path to native header files.
#include_directories()
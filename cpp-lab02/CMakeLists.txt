cmake_minimum_required(VERSION 3.5)
project(pw_lab08)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES main.cpp)
add_executable(pw_lab08 ${SOURCE_FILES})

find_package (Threads)
target_link_libraries (pw_lab08 ${CMAKE_THREAD_LIBS_INIT})
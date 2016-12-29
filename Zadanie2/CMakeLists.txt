cmake_minimum_required(VERSION 3.5)
project(Zadanie2)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14")

set(SOURCE_FILES brandes.cc main.cpp)
add_executable(Zadanie2 ${SOURCE_FILES})
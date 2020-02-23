cmake_minimum_required(VERSION 3.5)
project(Zadanie2)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -std=c++14 -lpthread")
find_package (Threads)

set(SOURCE_FILES logger.cc main.cc)
add_executable(Zadanie2 ${SOURCE_FILES})
target_link_libraries (Zadanie2 ${CMAKE_THREAD_LIBS_INIT})

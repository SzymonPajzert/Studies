cmake_minimum_required(VERSION 3.5)
project(Zadanie3)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")

set(SOURCE_FILES tree.c main.c)
add_executable(Zadanie3 ${SOURCE_FILES})
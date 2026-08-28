cmake_minimum_required(VERSION 4.1.2)
project("noxboard_native")

add_library(
        noxboard_native
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        noxboard_native
        ${log-lib}
        android)
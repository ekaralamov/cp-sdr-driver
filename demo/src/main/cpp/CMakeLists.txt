cmake_minimum_required(VERSION 3.4.1)

add_library(
    demo
    SHARED

    ../../../../opening/operations/rtl-sdr/src/rtl_fm.cpp
    )

find_package (oboe REQUIRED CONFIG)
target_link_libraries(demo oboe::oboe)

string(
    REGEX REPLACE
    "^.*/([^/]+)/obj/${ANDROID_ABI}$"
    "\\1"
    MY_VARIANT
    ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)
if (MY_VARIANT STREQUAL CMAKE_LIBRARY_OUTPUT_DIRECTORY)
    message(FATAL_ERROR "Variant determination failed. Probably the regex should be updated.")
endif ()


file(GLOB MY_CPP_API_IMPORTS "${MY_NATIVE_IMPORTS_ROOT}/${MY_VARIANT}/CppHeaders/*")
foreach (MY_CPP_API_IMPORT ${MY_CPP_API_IMPORTS})
    include_directories(${MY_CPP_API_IMPORT})
endforeach ()


file(GLOB MY_CPP_LIBNAMES_IMPORTS "${MY_NATIVE_IMPORTS_ROOT}/${MY_VARIANT}/LibNames/*")
foreach (MY_CPP_LIBNAMES_IMPORT ${MY_CPP_LIBNAMES_IMPORTS})
    file(STRINGS ${MY_CPP_LIBNAMES_IMPORT} MY_CPP_LIBNAMES)
    foreach (MY_CPP_LIBNAME ${MY_CPP_LIBNAMES})
        file(GLOB MY_CPP_RUNTIME_IMPORTS "${MY_NATIVE_IMPORTS_ROOT}/${MY_VARIANT}/Runtime/*/${ANDROID_ABI}/${MY_CPP_LIBNAME}")
        if (MY_CPP_RUNTIME_IMPORTS)
            list(APPEND MY_CPP_LIBNAMES_LIST ${MY_CPP_RUNTIME_IMPORTS})
        else ()
            list(APPEND MY_CPP_LIBNAMES_LIST ${MY_CPP_LIBNAME})
        endif ()
    endforeach ()
endforeach ()

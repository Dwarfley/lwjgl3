/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - SPIRV-Cross bindings")
    description("A library for performing reflection on SPIR-V and disassembling SPIR-V back to high level languages.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
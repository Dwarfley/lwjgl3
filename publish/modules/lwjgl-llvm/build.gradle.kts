/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - LLVM/Clang bindings")
    description("A collection of modular and reusable compiler and toolchain technologies.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
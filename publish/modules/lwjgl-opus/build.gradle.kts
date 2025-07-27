/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - Opus bindings")
    description("A totally open, royalty-free, highly versatile audio codec.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
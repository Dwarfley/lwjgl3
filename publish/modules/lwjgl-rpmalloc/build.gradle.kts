/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - rpmalloc bindings")
    description("A public domain cross platform lock free thread caching 16-byte aligned memory allocator implemented in C.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
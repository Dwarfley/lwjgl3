/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - spng bindings")
    description("libspng (simple png) is a C library for reading and writing Portable Network Graphics (PNG) format files with a focus on security and ease of use.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
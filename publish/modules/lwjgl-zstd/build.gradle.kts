/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - Zstandard bindings")
    description("A fast lossless compression algorithm, targeting real-time compression scenarios at zlib-level and better compression ratios.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
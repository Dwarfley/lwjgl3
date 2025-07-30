/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - LZ4 bindings")
    description("A lossless data compression algorithm that is focused on compression and decompression speed.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
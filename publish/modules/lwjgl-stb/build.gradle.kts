/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - stb bindings")
    description("Single-file public domain libraries for fonts, images, ogg vorbis files and more.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
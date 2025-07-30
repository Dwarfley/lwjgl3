/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - OpenAL bindings")
    description("A cross-platform 3D audio API appropriate for use with gaming applications and many other types of audio applications.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
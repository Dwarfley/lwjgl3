/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - FMOD bindings")
    description("An end-to-end solution for adding sound and music to any game.")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}
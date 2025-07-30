/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - xxHash bindings")
    description("An extremely fast hash algorithm, running at RAM speed limits.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
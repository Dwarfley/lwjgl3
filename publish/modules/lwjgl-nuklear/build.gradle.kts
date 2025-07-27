/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - Nuklear bindings")
    description("A minimal state immediate mode graphical user interface toolkit.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
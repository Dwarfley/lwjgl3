/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - JAWT bindings")
    description("The AWT native interface.")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}
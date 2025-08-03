/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - Native File Dialog bindings")
    description("A small C library that portably invokes native file open, folder select and file save dialogs.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
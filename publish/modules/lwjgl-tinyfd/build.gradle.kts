/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - Tiny File Dialogs bindings")
    description("Provides basic modal dialogs.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
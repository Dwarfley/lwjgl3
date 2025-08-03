/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - Tiny OpenEXR bindings")
    description("A small library to load and save OpenEXR(.exr) images.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
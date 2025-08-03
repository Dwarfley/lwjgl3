/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - OpenGL ES bindings")
    description("A royalty-free, cross-platform API for full-function 2D and 3D graphics on embedded systems - including consoles, phones, appliances and vehicles.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - EGL bindings")
    description("An interface between Khronos rendering APIs such as OpenGL ES or OpenVG and the underlying native platform window system.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}
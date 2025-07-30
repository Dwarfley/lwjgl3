/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - NanoVG & NanoSVG bindings")
    description("A small antialiased vector graphics rendering library for OpenGL. Also includes NanoSVG, a simple SVG parser.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - par_shapes bindings")
    description("Generate parametric surfaces and other simple shapes.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
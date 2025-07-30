/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - msdfgen bindings")
    description("Multi-channel signed distance field generator.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
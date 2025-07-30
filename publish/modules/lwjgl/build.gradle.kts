/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL")
    description("The LWJGL core library.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
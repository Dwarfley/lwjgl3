/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - Yoga bindings")
    description("An open-source, cross-platform layout library that implements Flexbox.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
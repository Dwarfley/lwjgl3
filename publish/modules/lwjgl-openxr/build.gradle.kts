/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - OpenXR bindings")
    description("A royalty-free, open standard that provides high-performance access to Augmented Reality (AR) and Virtual Reality (VR)—collectively known as XR—platforms and devices.")
    platforms {
        platform(FREEBSD, NATIVE_REQUIRED)
        platform(LINUX, NATIVE_REQUIRED)
        platform(WINDOWS, NATIVE_REQUIRED)
    }
}
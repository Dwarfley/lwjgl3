/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - OpenCL bindings")
    description("An open, royalty-free standard for cross-platform, parallel programming of diverse processors found in personal computers, servers, mobile devices and embedded platforms.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}
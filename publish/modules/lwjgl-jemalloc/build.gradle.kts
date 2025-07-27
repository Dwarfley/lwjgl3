/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - jemalloc bindings")
    description("A general purpose malloc implementation that emphasizes fragmentation avoidance and scalable concurrency support.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
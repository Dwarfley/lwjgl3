/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - Vulkan Memory Allocator bindings")
    description("An easy to integrate Vulkan memory allocation library.")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
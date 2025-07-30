/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - KTX (Khronos Texture) bindings")
    description("A lightweight container for textures for OpenGL®, Vulkan® and other GPU APIs.")
    platforms {
        platform(FREEBSD, NATIVE_REQUIRED)
        platform(LINUX, NATIVE_REQUIRED)
        platform(MACOS, NATIVE_REQUIRED)
        platform(WINDOWS_X64, NATIVE_REQUIRED)
        platform(WINDOWS_ARM64, NATIVE_REQUIRED)
    }
}
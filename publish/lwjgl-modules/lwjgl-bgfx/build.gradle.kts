/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - bgfx bindings")
    description("A cross-platform, graphics API agnostic rendering library. It provides a high performance, low level abstraction for common platform graphics APIs like OpenGL, Direct3D and Apple Metal.")
    dependsOn("lwjgl")
    platforms {
        platform(FREEBSD, NATIVE_REQUIRED)
        platform(LINUX, NATIVE_REQUIRED)
        platform(MACOS, NATIVE_REQUIRED)
        platform(WINDOWS_X64, NATIVE_REQUIRED)
        platform(WINDOWS_X86, NATIVE_REQUIRED)
    }
}
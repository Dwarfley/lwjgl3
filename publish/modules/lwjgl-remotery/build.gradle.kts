/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - Remotery bindings")
    description("A realtime CPU/GPU profiler hosted in a single C file with a viewer that runs in a web browser.")
    platforms {
        platform(FREEBSD, NATIVE_REQUIRED)
        platform(LINUX, NATIVE_REQUIRED)
        platform(MACOS, NATIVE_REQUIRED)
        platform(WINDOWS_X64, NATIVE_REQUIRED)
        platform(WINDOWS_X86, NATIVE_REQUIRED)
    }
}
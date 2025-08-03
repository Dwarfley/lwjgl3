/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - Shaderc bindings")
    description("A collection of libraries for shader compilation.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
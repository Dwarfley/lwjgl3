/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - FreeType bindings")
    description("A freely available software library to render fonts.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_REQUIRED)
    }
}
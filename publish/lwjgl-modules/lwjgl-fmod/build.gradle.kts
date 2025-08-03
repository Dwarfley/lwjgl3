/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication.createFromModule {
    title("LWJGL - FMOD bindings")
    description("An end-to-end solution for adding sound and music to any game.")
    dependsOn("lwjgl")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}
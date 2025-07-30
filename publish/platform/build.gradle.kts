/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-platform")
}

lwjglPublication.createFromPlatform {
    title("LWJGL BOM")
    description("LWJGL 3 Bill of Materials.")
}
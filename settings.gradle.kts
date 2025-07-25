/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
rootProject.name = "root"

includeBuild("build-logic")

include("modules")
project(":modules").projectDir = file("publish/modules")

include("platform")
project(":platform").projectDir = file("publish/platform")

val modulesDir = file("publish/modules/")

if (modulesDir.exists() && modulesDir.isDirectory) {
    modulesDir.listFiles { file -> file.isDirectory }.forEach { dir ->
        include("modules:${dir.name}")
        project(":${dir.name}").projectDir = dir
    }
}
/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
rootProject.name = "Root"

includeBuild("build-logic")

val modulesDir = file("bin/RELEASE/")

if (modulesDir.exists() && modulesDir.isDirectory) {
    modulesDir.listFiles { file -> file.isDirectory }.forEach { dir ->
        val moduleId = dir.name
        val moduleDir = file("bin/GRADLE/${moduleId}")

        if (!moduleDir.exists()) {
            moduleDir.mkdirs();
        }

        include(":${moduleId}")
        project(":${moduleId}").projectDir = moduleDir
    }
}
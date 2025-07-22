/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
rootProject.name = "Root"

includeBuild("build-logic")

val releaseDir = file("bin/RELEASE/")

if (releaseDir.exists() && releaseDir.isDirectory) {
    releaseDir.listFiles { file -> file.isDirectory }.forEach { dir ->
        val moduleId = dir.name
        val moduleDir = file("publish/${moduleId}")

        if (!moduleDir.exists()) {
            moduleDir.mkdirs();
        }

        include(":${moduleId}")
        project(":${moduleId}").projectDir = moduleDir
    }
}
/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    `kotlin-dsl`
}

val kotlinToolchainVersion: String by project

kotlin {
    jvmToolchain(kotlinToolchainVersion.toInt())
}

repositories {
    mavenCentral()
}

dependencies {
    project(":lwjgl-plugins").subprojects.forEach { pluginProject ->
        implementation(pluginProject)
    }
}
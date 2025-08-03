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
    implementation(project(":utils"))
}

gradlePlugin {
    plugins {
        create("lwjglComponent") {
            id = "lwjgl-component"
            implementationClass = "org.lwjgl.gradle.plugin.component.LwjglComponentPlugin"
        }
    }
}
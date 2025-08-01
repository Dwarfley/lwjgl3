/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-publishing")
}

val metadataTask = tasks.named("generateMetadata")

lwjglPublication {
    pom {
        packaging = "jar"
    }
}

configurations.register("metadata"){
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "metadata"))
    }
    outgoing.artifact(metadataTask){
        classifier = "metadata"
        extension = "txt"
        builtBy(metadataTask)
    }
}
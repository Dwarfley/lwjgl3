/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    `java-platform`
    id("lwjgl-publishing")
}

lwjglPublication {
    pom {
        packaging = "pom"
    }
}

val lwjglModules = mutableListOf<String>()

project(":modules").subprojects.forEach { subProject ->
    lwjglModules.add(subProject.name)
}

val metadataConfiguration = configurations.create("metadata"){
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "metadata"))
    }
}

dependencies {
    lwjglModules.forEach { module ->
        add(metadataConfiguration.name, project(":modules:${module}"))
    }
}

class NativeArtifact(
    val module: String,
    val classifier: String,
)

val nativeArtifacts = mutableListOf<NativeArtifact>()

metadataConfiguration.resolve().forEach { file ->
    file.readLines().forEach { line ->
        val parts = line.split(":")
        val id = parts[1]
        val classifier = parts[3]
        nativeArtifacts.add(NativeArtifact(id, classifier))
    }
}

lwjglPublication {
    pom {
        withXml {
            asElement().getElementsByTagName("dependencyManagement").item(0).apply {
                asElement().getElementsByTagName("dependencies").item(0).apply {
                    nativeArtifacts.forEach { artifact ->
                        ownerDocument.createElement("dependency").also(::appendChild).apply {
                            appendChild(ownerDocument.createElement("groupId").also(::appendChild).apply {
                                textContent = project.group as String
                            })
                            appendChild(ownerDocument.createElement("artifactId").also(::appendChild).apply {
                                textContent = artifact.module
                            })
                            appendChild(ownerDocument.createElement("version").also(::appendChild).apply {
                                textContent = project.version as String
                            })
                            appendChild(ownerDocument.createElement("classifier").also(::appendChild).apply {
                                textContent = artifact.classifier
                            })
                        }
                    }
                }
            }

            // Workaround for https://github.com/gradle/gradle/issues/7529
            asNode()
        }
    }
}

dependencies {
    constraints {
        lwjglModules.forEach { module ->
            api(project(":modules:${module}"))
        }
    }
}
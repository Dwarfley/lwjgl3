/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
import java.net.*

plugins {
    `java-platform`
    id("lwjgl-publishing")
    id("lwjgl-component-factory")
}

buildDir = file("bin/MAVEN")

enum class Platform(
    val os: String,
    val arch: String
) {
    NONE("os", "arch");

    val classifier: String
        get() {
            return ""
        }
}

enum class Module(
    val id: String,
    vararg val platforms: Platform
) {

    NONE(
        "id"
    );

    private fun path(buildDir: String) =
        "bin/${buildDir}/${id}"

    val isPresent
        get() = File(path("RELEASE")).exists()

    fun hasArtifact(classifier: String) =
        File("${path("RELEASE")}/${id}-${classifier}.jar").exists()

    fun getArtifact(classifier: String? = null) =
        if (classifier === null)
            File("${path("MAVEN")}/${id}.jar")
        else
            File("${path("MAVEN")}/${id}-${classifier}.jar")

}

publishing {
    publications {
        Module.values().forEach { module ->
            if (module.isPresent) {
                val moduleComponent = componentFactory.createComponent(module.id, module.getArtifact(), (project.version as String)) {
                    if (/*deployment.type !== BuildType.LOCAL ||*/ module.hasArtifact("sources")) {
                        sources(module.getArtifact("sources"))
                    }
                    if (/*deployment.type !== BuildType.LOCAL ||*/ module.hasArtifact("javadoc")) {
                        javadoc(module.getArtifact("javadoc"))
                    }

                    module.platforms.forEach { platform ->
                        if (/*deployment.type !== BuildType.LOCAL ||*/ module.hasArtifact(platform.classifier)) {
                            native(module.getArtifact(platform.classifier), platform.os, platform.arch, platform.classifier)
                        }
                    }
                }

                create<MavenPublication>(moduleComponent.name) {
                    artifactId = module.id
                    from(moduleComponent)
                }
            }
        }

        create<MavenPublication>("LwjglBom") {
            from(components["javaPlatform"])
            artifactId = "lwjgl-bom"

            pom {
                withXml {
                    asElement().getElementsByTagName("dependencyManagement").item(0).apply {
                        asElement().getElementsByTagName("dependencies").item(0).apply {
                            Module.values().forEach { module ->
                                module.platforms.forEach { platform ->
                                    ownerDocument.createElement("dependency").also(::appendChild).apply {
                                        appendChild(ownerDocument.createElement("groupId").also(::appendChild).apply {
                                            textContent = "org.lwjgl"
                                        })
                                        appendChild(ownerDocument.createElement("artifactId").also(::appendChild).apply {
                                            textContent = module.id
                                        })
                                        appendChild(ownerDocument.createElement("version").also(::appendChild).apply {
                                            textContent = project.version as String
                                        })
                                        appendChild(ownerDocument.createElement("classifier").also(::appendChild).apply {
                                            textContent = platform.classifier
                                        })
                                    }
                                }
                            }
                        }
                    }

                    // Workaround for https://github.com/gradle/gradle/issues/7529
                    asNode()
                }
            }
        }
    }
}

dependencies {
    constraints {
        Module.values().forEach { module ->
            api("org.lwjgl:${module.id}:${project.version}")
        }
    }
}
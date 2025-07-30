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
    FREEBSD_X64("freebsd", "x64"),
    LINUX_X64("linux", "x64"),
    LINUX_ARM64("linux", "arm64"),
    LINUX_ARM32("linux", "arm32"),
    LINUX_PPC64LE("linux", "ppc64le"),
    LINUX_RISCV64("linux", "riscv64"),
    MACOS_X64("macos", "x64"),
    MACOS_ARM64("macos", "arm64"),
    WINDOWS_X64("windows", "x64"),
    WINDOWS_X86("windows", "x86"),
    WINDOWS_ARM64("windows", "arm64");

    val classifier: String
        get() {
            return if (arch == "x64")
                "natives-${os}"
            else
                "natives-${os}-${arch}"
        }

    companion object {
        val ALL = values()
        val FREEBSD = arrayOf(FREEBSD_X64)
        val LINUX = arrayOf(LINUX_X64, LINUX_ARM64, LINUX_ARM32, LINUX_PPC64LE, LINUX_RISCV64)
        val MACOS = arrayOf(MACOS_X64, MACOS_ARM64)
        val WINDOWS = arrayOf(WINDOWS_X64, WINDOWS_X86, WINDOWS_ARM64)
    }
}

enum class Module(
    val id: String,
    val title: String,
    val description: String,
    vararg val platforms: Platform
) {

    NONE(
        "", "", "",
        *Platform.ALL
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
        fun MavenPom.setupPom(pomName: String, pomDescription: String, pomPackaging: String) {
            name.set(pomName)
            description.set(pomDescription)
            packaging = pomPackaging
        }

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

                    pom {
                        setupPom(module.title, module.description, "jar")
                    }
                }
            }
        }

        create<MavenPublication>("LwjglBom") {
            from(components["javaPlatform"])
            artifactId = "lwjgl-bom"

            pom {
                setupPom("LWJGL BOM", "LWJGL 3 Bill of Materials.", "pom")

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

val copyArchives = tasks.create<Copy>("copyArchives") {
    from("bin/RELEASE")
    include("**")
    destinationDir = buildDir
}

tasks.withType<Sign> {
    dependsOn(copyArchives)
}

dependencies {
    constraints {
        Module.values().forEach { module ->
            api("org.lwjgl:${module.id}:${project.version}")
        }
    }
}
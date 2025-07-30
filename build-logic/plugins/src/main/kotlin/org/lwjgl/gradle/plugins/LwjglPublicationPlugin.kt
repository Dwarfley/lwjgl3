/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.component.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.kotlin.dsl.*
import org.lwjgl.gradle.utils.*
import java.io.*
import javax.inject.*

class Platform internal constructor(
    internal val os: String,
    internal val arch: String
) {
    internal fun classifier(): String {
        return if (arch == "x64")
            "natives-${os}"
        else
            "natives-${os}-${arch}"
    }
}

class PlatformGroup internal constructor(
    internal vararg val platforms: Platform
) {
    internal fun forEach(action: (Platform) -> Unit) = platforms.forEach(action)
}

class NativeRequirement internal constructor(

) {

}

class PlatformConfigurator internal constructor(

) {
    val FREEBSD_X64: Platform = Platform("freebsd", "x64")
    val LINUX_X64: Platform = Platform("linux", "x64")
    val LINUX_ARM64: Platform = Platform("linux", "arm64")
    val LINUX_ARM32: Platform = Platform("linux", "arm32")
    val LINUX_PPC64LE: Platform = Platform("linux", "ppc64le")
    val LINUX_RISCV64: Platform = Platform("linux", "riscv64")
    val MACOS_X64: Platform = Platform("macos", "x64")
    val MACOS_ARM64: Platform = Platform("macos", "arm64")
    val WINDOWS_X64: Platform = Platform("windows", "x64")
    val WINDOWS_X86: Platform = Platform("windows", "x86")
    val WINDOWS_ARM64: Platform = Platform("windows", "arm64")

    val FREEBSD: PlatformGroup = PlatformGroup(FREEBSD_X64)
    val LINUX: PlatformGroup = PlatformGroup(LINUX_X64, LINUX_ARM64, LINUX_ARM32, LINUX_PPC64LE, LINUX_RISCV64)
    val MACOS: PlatformGroup = PlatformGroup(MACOS_X64, MACOS_ARM64)
    val WINDOWS: PlatformGroup = PlatformGroup(WINDOWS_X64, WINDOWS_X86, WINDOWS_ARM64)
    val ALL: PlatformGroup = PlatformGroup(*FREEBSD.platforms, *LINUX.platforms, *MACOS.platforms, *WINDOWS.platforms)

    val NATIVE_REQUIRED: NativeRequirement = NativeRequirement()
    val NATIVE_OPTIONAL: NativeRequirement = NativeRequirement()

    private val platformMap = mutableMapOf<Platform, NativeRequirement>()

    fun platform(platform: Platform, nativeRequirement: NativeRequirement) {
        platformMap.put(platform, nativeRequirement)
    }

    fun platform(platformGroup: PlatformGroup, nativeRequirement: NativeRequirement) {
        platformGroup.forEach { platform ->
            platform(platform, nativeRequirement)
        }
    }

    internal fun forEach(action: (Platform, Boolean) -> Unit){
        platformMap.forEach { platform, nativeRequirement ->
            action.invoke(platform, nativeRequirement == NATIVE_REQUIRED)
        }
    }
}

sealed class LwjglPublication {
    internal var title: String = ""
    internal var description: String = ""

    fun title(title: String) {
        this.title = title
    }

    fun description(description: String) {
        this.description = description
    }
}

class ModulePublication : LwjglPublication() {
    internal val platforms: PlatformConfigurator = PlatformConfigurator()

    fun platforms(action: Action<PlatformConfigurator>) {
        action.execute(this.platforms)
    }
}

class PlatformPublication : LwjglPublication() {

}

internal enum class PublicationType {
    LOCAL,
    SNAPSHOT,
    RELEASE
}

open class LwjglPublicationExtension constructor(
    private val project: Project,
    private val softwareComponentFactory: SoftwareComponentFactory
) {
    private var publicationType: PublicationType = PublicationType.LOCAL
    private val pomActions = mutableListOf<Action<MavenPom>>()

    fun local(){
        publicationType = PublicationType.LOCAL
    }

    fun snapshot(){
        publicationType = PublicationType.SNAPSHOT
    }

    fun release(){
        publicationType = PublicationType.RELEASE
    }

    fun pom(action: Action<MavenPom>) {
        pomActions.add(action)
    }

    fun createFromModule(action: Action<ModulePublication>) {
        if(!isPresent()){
            return
        }

        val publication = ModulePublication()

        action.execute(publication)

        create(project.name.toPascalCase(), publication) {
            artifactId = project.name

            val component = softwareComponentFactory.adhoc("lwjglModule")

            if (publicationType != PublicationType.LOCAL || hasArtifact("sources")) {
                /*sources(getArtifact("sources"))*/
            }
            if (publicationType != PublicationType.LOCAL || hasArtifact("javadoc")) {
                /*javadoc(getArtifact("javadoc"))*/
            }

            publication.platforms.forEach { platform, isNativeRequired ->
                if (isNativeRequired && (publicationType != PublicationType.LOCAL || hasArtifact(platform.classifier()))) {
                    /*native(getArtifact(platform.classifier()), platform.os, platform.arch, platform.classifier())*/
                }
            }

            from(component)
        }
    }

    fun createFromPlatform(action: Action<PlatformPublication>) {
        val publication = PlatformPublication()

        action.execute(publication)

        create(project.name.toPascalCase(), publication) {
            artifactId = "lwjgl-bom"

            from(project.components["javaPlatform"])
        }
    }

    private fun create(name: String, publication: LwjglPublication, action: Action<MavenPublication>) {
        project.extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>(name) {
                    action.execute(this)
                    pom {
                        this.name.set(publication.title)
                        this.description.set(publication.description)
                        pomActions.forEach { action ->
                            action.execute(this)
                        }
                    }
                }
            }
        }
    }

    private fun isPresent(): Boolean {
        return getFile("").exists()
    }

    private fun hasArtifact(classifier: String): Boolean {
        return getFile("${project.name}-${classifier}.jar").exists()
    }

    private fun getArtifact(classifier: String? = null): File {
        return if (classifier === null) {
            getFile("${project.name}.jar")
        } else {
            getFile("${project.name}-${classifier}.jar")
        }
    }

    private fun getFile(path: String): File {
        val root = project.rootProject.layout.projectDirectory.asFile
        return root.resolve("bin/RELEASE/${project.name}/$path")
    }
}

class LwjglPublicationPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(MavenPublishPlugin::class.java)

        project.extensions.create(
            LwjglPublicationExtension::class.java,
            "lwjglPublication",
            LwjglPublicationExtension::class.java,
            project,
            softwareComponentFactory
        )
    }
}
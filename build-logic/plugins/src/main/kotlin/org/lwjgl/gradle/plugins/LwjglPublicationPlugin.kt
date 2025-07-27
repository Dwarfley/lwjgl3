/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.component.*
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

    fun platform(platform: Platform, nativeRequirement: NativeRequirement) {

    }

    fun platform(platformGroup: PlatformGroup, nativeRequirement: NativeRequirement) {
        platformGroup.forEach { platform ->
            platform(platform, nativeRequirement)
        }
    }
}

open class LwjglPublicationExtension constructor(
    private val project: Project,
    private val softwareComponentFactory: SoftwareComponentFactory
) {
    private val platformConfigurator: PlatformConfigurator = PlatformConfigurator()

    fun title(title: String) {

    }

    fun description(description: String) {

    }

    fun platforms(action: Action<PlatformConfigurator>) {
        action.execute(platformConfigurator)
    }
}

class LwjglPublicationPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            LwjglPublicationExtension::class.java,
            "lwjglPublication",
            LwjglPublicationExtension::class.java,
            project,
            softwareComponentFactory
        )
    }
}
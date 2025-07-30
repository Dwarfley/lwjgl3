/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.kotlin.dsl.*
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.lwjgl.gradle.utils.*
import java.io.*
import javax.inject.*
import kotlin.jvm.java

internal enum class VariantType(
    val containsJavaBinary: Boolean,
    private val usage: String,
    private val category: String,
    private val bundling: String,
    private val docsType: String?,
    private val libraryElements: String?
) {
    COMPILE(true, Usage.JAVA_API, Category.LIBRARY, Bundling.EXTERNAL, null, LibraryElements.JAR),
    RUNTIME(true, Usage.JAVA_RUNTIME, Category.LIBRARY, Bundling.EXTERNAL, null, LibraryElements.JAR),
    NATIVE_COMPILE(true, Usage.JAVA_API, Category.LIBRARY, Bundling.EXTERNAL, null, LibraryElements.JAR),
    NATIVE_RUNTIME(true, Usage.JAVA_RUNTIME, Category.LIBRARY, Bundling.EXTERNAL, null, LibraryElements.JAR),
    NATIVE_ONLY(false, Usage.NATIVE_RUNTIME, Category.LIBRARY, Bundling.EXTERNAL, null, LibraryElements.JAR),
    JAVADOC(false, Usage.JAVA_RUNTIME, Category.DOCUMENTATION, Bundling.EXTERNAL, DocsType.JAVADOC, null),
    SOURCES(false, Usage.JAVA_RUNTIME, Category.DOCUMENTATION, Bundling.EXTERNAL, DocsType.SOURCES, null);

    fun applyAttributes(objects: ObjectFactory, configuration: Configuration) {
        configuration.attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, usage))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, category))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, bundling))
            docsType?.let {
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType::class.java, it))
            }
            libraryElements?.let {
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, it))
            }
        }
    }
}

internal class ComponentConfigurator constructor(
    private val project: Project,
    private val component: AdhocComponentWithVariants,
) {
    fun main(artifact: File) {
        createConfiguration("Api", VariantType.COMPILE) {
            outgoing.artifact(artifact)
        }
        createConfiguration("Runtime", VariantType.RUNTIME) {
            outgoing.artifact(artifact)
        }
    }

    fun native(os: String, arch: String) {

    }

    fun native(artifact: File, os: String, arch: String, classifier: String) {
        val platform = "${os.toPascalCase()}${arch.toPascalCase()}"

        fun addAttributes(configuration: Configuration) {
            configuration.attributes {
                attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, project.objects.named(OperatingSystemFamily::class.java, os))
                attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, project.objects.named(MachineArchitecture::class.java, arch))
            }
        }

        createConfiguration("${platform}Api", VariantType.NATIVE_COMPILE) {
            addAttributes(this)
            /*outgoing.artifact(mainArtifact)*/
            outgoing.artifact(artifact) {
                this.classifier = classifier
            }
        }
        createConfiguration("${platform}Runtime", VariantType.NATIVE_RUNTIME) {
            addAttributes(this)
            /*outgoing.artifact(mainArtifact)*/
            outgoing.artifact(artifact) {
                this.classifier = classifier
            }
        }
        createConfiguration("${platform}Native", VariantType.NATIVE_ONLY) {
            addAttributes(this)
            outgoing.artifact(artifact) {
                this.classifier = classifier
            }
        }
    }

    fun sources(artifact: File) {
        createConfiguration("Sources", VariantType.SOURCES) {
            outgoing.artifact(artifact) {
                this.classifier = "sources"
            }
        }
    }

    fun javadoc(artifact: File) {
        createConfiguration("Javadoc", VariantType.JAVADOC) {
            outgoing.artifact(artifact) {
                this.classifier = "javadoc"
            }
        }
    }

    private fun createConfiguration(name: String, type: VariantType, configAction: Action<Configuration>) {
        val configuration = project.configurations.create("${project.name.toCamelCase()}${name}Elements") {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                if (type.containsJavaBinary) {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
                attribute(Attribute.of("org.lwjgl.module", String::class.java), project.name)
            }
            type.applyAttributes(project.objects, this)
            if (project.name != "lwjgl" && type.containsJavaBinary) {
                dependencies.add(project.dependencies.create("org.lwjgl:lwjgl:${project.version}"))
            }
        }

        configAction.execute(configuration)

        component.addVariantsFromConfiguration(configuration) {
            if (type == VariantType.COMPILE) {
                mapToMavenScope("compile")
            } else if (type == VariantType.RUNTIME) {
                mapToMavenScope("runtime")
            }
        }
    }
}

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

class NativeRequirement internal constructor()

class PlatformConfigurator internal constructor() {
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

sealed class LwjglPublication protected constructor() {
    internal var title: String = ""
    internal var description: String = ""

    fun title(title: String) {
        this.title = title
    }

    fun description(description: String) {
        this.description = description
    }
}

class ModulePublication internal constructor() : LwjglPublication() {
    internal val platforms: PlatformConfigurator = PlatformConfigurator()

    fun platforms(action: Action<PlatformConfigurator>) {
        action.execute(this.platforms)
    }
}

class PlatformPublication internal constructor() : LwjglPublication()

internal enum class PublicationType {
    LOCAL,
    SNAPSHOT,
    RELEASE
}

open class LwjglPublicationExtension constructor(
    private val project: Project,
    private val softwareComponentFactory: SoftwareComponentFactory
) {
    private var publicationType: PublicationType? = null
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
        val publication = ModulePublication()

        action.execute(publication)

        if(!isPresent()){
            return
        }

        createMavenPublication(project.name.toPascalCase(), publication) {
            artifactId = project.name

            val component = createComponent(){
                val isLocal = publicationType == PublicationType.LOCAL

                main(getArtifact())

                if (!isLocal || hasArtifact("sources")) {
                    sources(getArtifact("sources"))
                }
                if (!isLocal || hasArtifact("javadoc")) {
                    javadoc(getArtifact("javadoc"))
                }

                publication.platforms.forEach { platform, isNativeRequired ->
                    if (!isLocal || hasArtifact(platform.classifier())) {
                        if(isNativeRequired){
                            native(getArtifact(platform.classifier()), platform.os, platform.arch, platform.classifier())
                        }else{
                            native(platform.os, platform.arch)
                        }
                    }
                }
            }

            from(component)
        }
    }

    fun createFromPlatform(action: Action<PlatformPublication>) {
        val publication = PlatformPublication()

        action.execute(publication)

        createMavenPublication(project.name.toPascalCase(), publication) {
            artifactId = "lwjgl-bom"

            from(project.components["javaPlatform"])
        }
    }

    private fun createComponent(action: Action<ComponentConfigurator>): AdhocComponentWithVariants {
        val component = softwareComponentFactory.adhoc("lwjgl")
        val configurator = ComponentConfigurator(project, component)

        action.execute(configurator)

        return component
    }

    private fun createMavenPublication(name: String, publication: LwjglPublication, action: Action<MavenPublication>) {
        project.extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>(name) {
                    pom {
                        this.name.set(publication.title)
                        this.description.set(publication.description)
                        pomActions.forEach { action ->
                            action.execute(this)
                        }
                    }

                    action.execute(this)
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
        return if (classifier == null) {
            getFile("${project.name}.jar")
        } else {
            getFile("${project.name}-${classifier}.jar")
        }
    }

    private fun getFile(path: String): File {
        val root = project.rootProject.layout.projectDirectory.asFile
        return root.resolve("bin/RELEASE/${project.name}/${path}")
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
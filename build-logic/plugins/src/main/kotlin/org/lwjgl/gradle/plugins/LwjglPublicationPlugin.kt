/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
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

internal class ComponentConfigurator constructor(
    private val project: Project,
    private val objects: ObjectFactory,
    private val component: AdhocComponentWithVariants,
    private val moduleDependencies: MutableList<String>,
) {
    private val nativeCapability: String = "${project.group}:${project.name}-native:${project.version}"

    fun main(artifact: File) {
        createMainVariant("api", "compile") {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_API))
            }
            outgoing.artifact(artifact)
        }
        createMainVariant("runtime", "runtime") {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }
            outgoing.artifact(artifact)
        }
    }

    fun native(os: String, arch: String) {
        native(null, os, arch, null)
    }

    fun native(artifact: File?, os: String, arch: String, classifier: String?) {
        createNativeVariant("api", os, arch) {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_API))
            }
            if(artifact != null && classifier != null){
                outgoing.artifact(artifact){
                    this.classifier = classifier
                }
            }
        }
        createNativeVariant("runtime", os, arch) {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }
            if(artifact != null && classifier != null){
                outgoing.artifact(artifact){
                    this.classifier = classifier
                }
            }
        }
    }

    fun sources(artifact: File) {
        createDocumentationVariant("sources") {
            attributes {
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType::class.java, DocsType.SOURCES))
            }
            outgoing.artifact(artifact) {
                this.classifier = "sources"
            }
        }
    }

    fun javadoc(artifact: File) {
        createDocumentationVariant("javadoc") {
            attributes {
                attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType::class.java, DocsType.JAVADOC))
            }
            outgoing.artifact(artifact) {
                this.classifier = "javadoc"
            }
        }
    }

    private fun createMainVariant(name: String, mavenScope: String, action: Action<Configuration>) {
        createVariant(name, mavenScope){
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, LibraryElements.JAR))
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            }
            action.execute(this)
        }
        project.dependencies{
            add(configurationName(name), "${project.group}:${project.name}:${project.version}") {
                capabilities {
                    requireCapability(nativeCapability)
                }
            }
            moduleDependencies.forEach { module ->
                add(configurationName(name), project(":modules:${module}"))
            }
        }
    }

    private fun createNativeVariant(name: String, os: String, arch: String, action: Action<Configuration>) {
        createVariant("${os.toCamelCase()}${arch.toPascalCase()}${name.toPascalCase()}"){
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, LibraryElements.JAR))
                attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, project.objects.named(OperatingSystemFamily::class.java, os))
                attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, project.objects.named(MachineArchitecture::class.java, arch))
            }
            outgoing.capability(nativeCapability)
            action.execute(this)
        }
    }

    private fun createDocumentationVariant(name: String, action: Action<Configuration>) {
        createVariant(name){
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.DOCUMENTATION))
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
            }
            action.execute(this)
        }
    }

    private fun createVariant(name: String, mavenScope: String? = null, configAction: Action<Configuration>) {
        val configuration = project.configurations.create(configurationName(name)) {
            isCanBeResolved = false
            isCanBeConsumed = true
        }

        configAction.execute(configuration)

        component.addVariantsFromConfiguration(configuration) {
            if (mavenScope != null) {
                mapToMavenScope(mavenScope)
            }
        }
    }

    private fun configurationName(name: String): String {
        return "${name}Elements"
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
    internal val moduleDependencies = mutableListOf<String>()
    internal val platforms: PlatformConfigurator = PlatformConfigurator()

    fun dependsOn(module: String) {
        moduleDependencies.add(module)
    }

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

        val isLocal = publicationType == PublicationType.LOCAL

        val nativeArtifacts = mutableListOf<String>()

        publication.platforms.forEach { platform, isNativeRequired ->
            if (!isLocal || hasArtifact(platform.classifier())) {
                if(isNativeRequired){
                    nativeArtifacts.add("${project.group}:${project.name}:${project.version}:${platform.classifier()}")
                }
            }
        }

        val component = createComponent(publication.moduleDependencies) {
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

        createMavenPublication(project.name.toPascalCase(), publication) {
            artifactId = project.name

            suppressAllPomMetadataWarnings()

            from(component)
        }

        project.tasks.named("generateMetadata") {
            val outputFile = project.layout.buildDirectory.file("generated/metadata.txt")
            val text = nativeArtifacts.joinToString("\n")

            outputs.file(outputFile)

            doLast {
                val file = outputFile.get().asFile
                file.parentFile.mkdirs()
                file.writeText(text)
            }
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

    private fun createComponent(moduleDependencies: MutableList<String>, action: Action<ComponentConfigurator>): AdhocComponentWithVariants {
        val component = softwareComponentFactory.adhoc("lwjgl")
        val configurator = ComponentConfigurator(project, project.objects, component, moduleDependencies)

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

        project.tasks.register("generateMetadata")
    }
}
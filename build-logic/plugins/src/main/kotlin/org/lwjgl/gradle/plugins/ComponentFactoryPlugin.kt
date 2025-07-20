/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.*
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.*
import org.gradle.api.model.*
import org.gradle.nativeplatform.*
import org.lwjgl.gradle.utils.*
import java.io.*
import javax.inject.*

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

interface ComponentConfigurator {
    fun native(artifact: File, os: String, arch: String, classifier: String)
    fun sources(artifact: File)
    fun javadoc(artifact: File)
}

internal class DefaultComponentConfigurator constructor(
    private val project: Project,
    private val component: AdhocComponentWithVariants,
    private val id: String,
    private val mainArtifact: File,
    private val version: String,
    private val isCore: Boolean
) : ComponentConfigurator {
    init {
        createConfiguration("Api", VariantType.COMPILE) {
            outgoing.artifact(mainArtifact)
        }
        createConfiguration("Runtime", VariantType.RUNTIME) {
            outgoing.artifact(mainArtifact)
        }
    }

    override fun native(artifact: File, os: String, arch: String, classifier: String) {
        val platform = "${os.toPascalCase()}${arch.toPascalCase()}"

        fun addAttributes(configuration: Configuration) {
            configuration.attributes {
                attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, project.objects.named(OperatingSystemFamily::class.java, os))
                attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, project.objects.named(MachineArchitecture::class.java, arch))
            }
        }

        createConfiguration("${platform}Api", VariantType.NATIVE_COMPILE) {
            addAttributes(this)
            outgoing.artifact(mainArtifact)
            outgoing.artifact(artifact) {
                this.classifier = classifier
            }
        }
        createConfiguration("${platform}Runtime", VariantType.NATIVE_RUNTIME) {
            addAttributes(this)
            outgoing.artifact(mainArtifact)
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

    override fun sources(artifact: File) {
        createConfiguration("Sources", VariantType.SOURCES) {
            outgoing.artifact(artifact) {
                this.classifier = "sources"
            }
        }
    }

    override fun javadoc(artifact: File) {
        createConfiguration("Javadoc", VariantType.JAVADOC) {
            outgoing.artifact(artifact) {
                this.classifier = "javadoc"
            }
        }
    }

    private fun createConfiguration(name: String, type: VariantType, configAction: Action<Configuration>) {
        val configuration = project.configurations.create("${id.toCamelCase()}${name}Elements") {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                if (type.containsJavaBinary) {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
                attribute(Attribute.of("org.lwjgl.module", String::class.java), id)
            }
            type.applyAttributes(project.objects, this)
            if (!isCore && type.containsJavaBinary) {
                dependencies.add(project.dependencies.create("org.lwjgl:lwjgl:${version}"))
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

open class ComponentFactoryExtension constructor(
    private val project: Project,
    private val softwareComponentFactory: SoftwareComponentFactory
) {
    fun createComponent(id: String, artifact: File, version: String, action: Action<ComponentConfigurator>): AdhocComponentWithVariants {
        val component = softwareComponentFactory.adhoc(id.toPascalCase())
        val configurator = DefaultComponentConfigurator(project, component, id, artifact, version, id == "lwjgl")

        action.execute(configurator)

        return component
    }
}

class ComponentFactoryPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            ComponentFactoryExtension::class.java,
            "componentFactory",
            ComponentFactoryExtension::class.java,
            project,
            softwareComponentFactory
        )
    }
}
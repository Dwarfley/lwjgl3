
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
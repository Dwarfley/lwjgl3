/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    `java-platform`
    id("lwjgl-publishing")
}

lwjglPublication.all {
    from(components["javaPlatform"])

    pom {
        packaging = "pom"
    }
}

val lwjglModules = mutableListOf<String>()

project(":lwjgl-modules").subprojects.forEach { subProject ->
    lwjglModules.add(subProject.name)
}

val metadataConfiguration: Configuration = configurations.create("metadata") {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "metadata"))
    }
}

dependencies {
    lwjglModules.forEach { module ->
        add(metadataConfiguration.name, project(":lwjgl-modules:${module}"))
    }
}

tasks.withType<GenerateMavenPom>().configureEach {
    val metadataFiles: FileCollection = metadataConfiguration

    inputs.files(metadataFiles)

    doLast {
        val artifacts = mutableMapOf<String, MutableList<String>>()

        metadataFiles.forEach { file ->
            file.readLines().forEach { line ->
                val parts = line.split(":")

                val id = parts[1]
                val classifier = parts[3]

                artifacts.getOrPut(id) {
                    mutableListOf<String>("")
                }.add(classifier)
            }
        }

        fun regex(regex: String): Regex {
            return Regex(regex, RegexOption.DOT_MATCHES_ALL)
        }

        fun separator(text: String, leftTag: String, rightTag: String): String {
            return regex("</($leftTag)>(.*?)<($rightTag)>").find(text)?.groupValues[2] ?: ""
        }

        val text = destination.readText()

        val separator: String = separator(text, "dependency", "dependency")

        val resultText = regex("<dependency>.*?</dependency>").replace(text) { result ->
            val dependencyText = result.value

            val classifierSeparator: String = separator(dependencyText, "groupId|artifactId|version", "groupId|artifactId|version")
            val id = regex("<artifactId>(.*?)</artifactId>").find(dependencyText)?.groupValues[1] ?: ""

            (artifacts[id] ?: listOf("")).joinToString(separator) { classifier ->
                if (classifier != "") {
                    regex("</version>").replace(dependencyText, "$0${classifierSeparator}<classifier>${classifier}</classifier>")
                } else {
                    dependencyText
                }
            }
        }

        destination.writeText(resultText)
    }
}

dependencies {
    constraints {
        lwjglModules.forEach { module ->
            api(project(":lwjgl-modules:${module}"))
        }
    }
}
/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
/*
publishing {
    publications {
        create<MavenPublication>("LwjglBom") {
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
*/
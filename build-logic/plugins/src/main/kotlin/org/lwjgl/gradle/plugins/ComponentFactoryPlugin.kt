/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugins

import org.gradle.api.*
import org.gradle.api.component.*
import javax.inject.*

open class ComponentFactoryExtension constructor(
    private val project: Project,
    private val softwareComponentFactory: SoftwareComponentFactory
) {

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
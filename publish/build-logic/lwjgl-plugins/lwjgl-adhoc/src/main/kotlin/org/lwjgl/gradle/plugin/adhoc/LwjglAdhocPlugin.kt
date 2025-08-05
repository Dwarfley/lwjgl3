/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package org.lwjgl.gradle.plugin.adhoc

import org.gradle.api.*
import org.gradle.api.component.SoftwareComponentFactory
import javax.inject.Inject

open class LwjglAdhocExtension(
    private val project: Project
) {

}

class LwjglAdhocPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            LwjglAdhocExtension::class.java,
            "lwjglAdhoc",
            LwjglAdhocExtension::class.java,
            project
        )
    }
}
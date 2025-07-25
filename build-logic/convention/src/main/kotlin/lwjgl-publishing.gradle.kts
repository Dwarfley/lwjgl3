/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    `maven-publish`
    signing
}

val lwjglVersion: String by project
val lwjglGroup: String by project

val signingKeyId: String? by project
val signingKey: String? by project
val signingPassword: String? by project

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

val releaseRepo: String? by project
val snapshotRepo: String? by project

enum class PublishType {
    LOCAL,
    SNAPSHOT,
    RELEASE;

    override fun toString(): String = name.lowercase()
}

val publishType: PublishType = when {
    hasProperty("release") -> PublishType.RELEASE
    hasProperty("snapshot") -> PublishType.SNAPSHOT
    else -> PublishType.LOCAL
}

logger.info("Publishing to {} repository", publishType)

data class SigningCredentials(
    val keyId: String,
    val key: String,
    val password: String
)

data class PublishingCredentials(
    val username: String,
    val password: String
)

val signingCredentials: SigningCredentials? = signingKeyId?.let { keyId ->
    signingKey?.let { key ->
        signingPassword?.let { password ->
            SigningCredentials(keyId, key, password)
        }
    }
}

val publishingCredentials: PublishingCredentials? = sonatypeUsername?.let { username ->
    sonatypePassword?.let { password ->
        PublishingCredentials(username, password)
    }
}

val publishRepository: String = when (publishType) {
    PublishType.RELEASE -> releaseRepo ?: ""
    PublishType.SNAPSHOT -> snapshotRepo ?: ""
    else -> ""
}

if (publishType != PublishType.LOCAL && signingCredentials == null) {
    throw GradleException("Missing required properties 'signingKeyId', 'signingKey' and 'signingPassword' for '$publishType' publishing")
}

if (publishType != PublishType.LOCAL && publishingCredentials == null) {
    throw GradleException("Missing required properties 'sonatypeUsername' and 'sonatypePassword' for '$publishType' publishing")
}

if (publishType == PublishType.RELEASE && releaseRepo == null) {
    throw GradleException("Missing required property 'releaseRepo' for '$publishType' publishing")
}

if (publishType == PublishType.SNAPSHOT && snapshotRepo == null) {
    throw GradleException("Missing required property 'snapshotRepo' for '$publishType' publishing")
}

defaultTasks = mutableListOf("publish")

group = lwjglGroup
version = when (publishType) {
    PublishType.RELEASE -> lwjglVersion
    PublishType.SNAPSHOT -> "$lwjglVersion-SNAPSHOT"
    PublishType.LOCAL -> "$lwjglVersion-SNAPSHOT"
}

publishing {
    repositories {
        if (publishType != PublishType.LOCAL) {
            maven {
                url = uri(publishRepository)

                if (publishingCredentials != null) {
                    credentials {
                        username = publishingCredentials.username
                        password = publishingCredentials.password
                    }
                }
            }
        } else {
            mavenLocal()
        }
    }
}

signing {
    setRequired({
        signingCredentials != null
    })
    if (signingCredentials != null) {
        useInMemoryPgpKeys(
            signingCredentials.keyId,
            signingCredentials.key,
            signingCredentials.password
        )
    }
    sign(publishing.publications)
}
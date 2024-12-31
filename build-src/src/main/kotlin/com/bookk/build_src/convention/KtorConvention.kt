package com.bookk.build_src.convention

import io.ktor.plugin.features.DockerExtension
import io.ktor.plugin.features.DockerImageRegistry
import io.ktor.plugin.features.KtorExtension
import io.ktor.plugin.features.getExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

fun KtorExtension.applyConvention(project: Project) {
    getExtension<DockerExtension>().apply {
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set(project.provider { project.group.toString() })
        imageTag.set(project.provider { project.version.toString() })
        externalRegistry.set(
            DockerImageRegistry.externalRegistry(
                project = project.provider { project.group.toString() },
                namespace = project.provider { "an22" },
                hostname = project.provider { "ghcr.io" },
                username = project.provider {
                    System.getenv("DOCKER_USERNAME").orEmpty().ifBlank {
                        project.property("DOCKER_USERNAME").toString()
                    }
                },
                password = project.provider {
                    System.getenv("DOCKER_PASSWORD").orEmpty().ifBlank {
                        project.property("DOCKER_PASSWORD").toString()
                    }
                }
            )
        )
    }
}
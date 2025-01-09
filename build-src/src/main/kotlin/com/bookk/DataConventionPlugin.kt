package com.bookk

import com.bookk.build_src.convention.applyConvention
import com.bookk.build_src.tools.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@Suppress("unused")
class DataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(libs.plugins.kotlin.jvm.get().pluginId)
            }

            extensions.getByType<JavaPluginExtension>().applyConvention()
            extensions.getByType<KotlinJvmProjectExtension>().applyConvention()
            target.tasks.withType(Test::class.java) {
                useJUnitPlatform()
            }

            target.dependencies {
                add("implementation", libs.exposed.core)
                add("implementation", libs.exposed.dao)
                add("implementation", libs.exposed.migration)
                add("implementation", libs.exposed.jdbc)
                add("implementation", libs.hikari.cp)
                add("implementation", libs.koin.core)
                add("implementation", libs.flyway.core)
                add("implementation", libs.flyway.mysql)
                add("implementation", platform(libs.koin.bom))
                add("implementation", libs.mariadb)
                add("implementation", libs.kotlin.coroutines)
                add("testImplementation", libs.kotlin.test)
            }
        }
    }
}
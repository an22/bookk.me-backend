package com.bookk.build_src.convention

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal fun KotlinJvmProjectExtension.applyConvention() {
    jvmToolchain(21)
    sourceSets.all {
        languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
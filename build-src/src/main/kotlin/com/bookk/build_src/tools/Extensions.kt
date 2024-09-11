package com.bookk.build_src.tools

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import java.util.*

fun Project.findStringProperty(key: String, fileName: String): String {
    val propertiesFile = project.rootProject.file(fileName)
    val properties = Properties()
    properties.load(propertiesFile.inputStream())
    return properties.getProperty(key)?.toString() ?: throw GradleException("$key not found in $fileName")
}

val Project.nativeClassifier: String?
    get() {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> "windows-x86_64"
            osName.contains("linux") -> "linux-x86_64"
            osName.contains("mac") -> "osx-x86_64"
            else -> null
        }
    }

val Project.libs: LibrariesForLibs
    get() = (this as ExtensionAware).extensions.getByName("libs") as LibrariesForLibs
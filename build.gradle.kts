import com.bookk.build_src.tools.includeLocalProperties
import com.google.cloud.tools.jib.gradle.JibTask

plugins {
    alias(libs.plugins.ktor).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.bookk.data).apply(false)
    alias(libs.plugins.bookk.domain.api).apply(false)
    alias(libs.plugins.bookk.domain.impl).apply(false)
    alias(libs.plugins.bookk.client).apply(false)
    alias(libs.plugins.bookk.microservice)
}

group = "com.bookk.server"
version = "0.0.1"

application {
    mainClass.set("com.bookk.server.MonolithServerKt")
}

dependencies {
    implementation(projects.core.service)
    implementation(projects.service.authorization.microservice)
    implementation(projects.service.user.microservice)
}

includeLocalProperties(providers.gradleProperty("local.propertiesFile").get())

tasks.withType<JibTask> {
    notCompatibleWithConfigurationCache("because of https://github.com/GoogleContainerTools/jib/issues/3132")
}
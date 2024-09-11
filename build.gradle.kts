plugins {
    alias(libs.plugins.ktor).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.book.data).apply(false)
    alias(libs.plugins.book.domain.api).apply(false)
    alias(libs.plugins.book.domain.impl).apply(false)
    alias(libs.plugins.book.microservice)
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
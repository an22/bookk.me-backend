plugins {
    alias(libs.plugins.bookk.microservice)
}

group = "com.bookk.core.service"
version = "0.0.1"

application {
    mainClass.set("com.book.core.MockMainClassKt")
}

dependencies {
    implementation(libs.ktor.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.logging.call)
    implementation(projects.core)
    implementation(projects.core.data.cache.api)
}
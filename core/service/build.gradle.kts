plugins {
    alias(libs.plugins.book.microservice)
}

dependencies {
    implementation(libs.ktor.json)
    implementation(libs.ktor.logging)
    implementation(projects.core)
    implementation(projects.core.data.cache.api)
}
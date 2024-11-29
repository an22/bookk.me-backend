plugins {
    alias(libs.plugins.bookk.microservice)
}

dependencies {
    implementation(libs.ktor.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.logging.call)
    implementation(projects.core)
    implementation(projects.core.data.cache.api)
}
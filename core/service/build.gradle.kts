plugins {
    alias(libs.plugins.book.microservice)
}

dependencies {
    implementation(libs.ktor.json)
    implementation(projects.core.data.cache.api)
}
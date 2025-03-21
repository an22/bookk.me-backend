plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.cache.api)
    implementation(projects.core.data.eventstreaming.api)
    implementation(libs.ktor.idempotency)
}
plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.cache.api)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.library.idempotency)
}
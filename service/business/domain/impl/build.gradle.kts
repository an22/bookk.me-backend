plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.jwt)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.service.business.domain.api)
    implementation(projects.service.business.client)
    implementation(projects.service.authorization.client)
    implementation(projects.service.business.data.source)
    implementation(projects.library.permissions)
}
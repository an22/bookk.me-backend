plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.ktor.jwt)
    implementation(libs.passkey)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.service.authorization.domain.api)
    implementation(projects.service.authorization.domain.datasource)
    implementation(projects.service.user.domain.api)
    implementation(projects.service.user.client)
}
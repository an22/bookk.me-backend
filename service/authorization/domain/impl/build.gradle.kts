plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.jwt)
    implementation(libs.passkey)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.service.authorization.data)
    implementation(projects.service.authorization.domain.api)
    implementation(projects.service.user.domain.api)
    implementation(projects.service.user.client)
}
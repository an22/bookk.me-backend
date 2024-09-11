plugins {
    alias(libs.plugins.book.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.jwt)
    implementation(libs.totp)
    implementation(libs.apache.base32)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.service.authorization.data)
    implementation(projects.service.authorization.domain.api)
    implementation(projects.service.user.domain.api)
    implementation(projects.service.user.client)
}
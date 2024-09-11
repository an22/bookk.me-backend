plugins {
    alias(libs.plugins.book.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.jwt)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.service.user.data)
    implementation(projects.service.user.domain.api)
}
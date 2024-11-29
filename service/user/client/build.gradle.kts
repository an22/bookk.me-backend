plugins {
    alias(libs.plugins.bookk.client)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.domain)
    api(projects.service.user.domain.api)
}
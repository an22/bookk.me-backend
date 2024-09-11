plugins {
    alias(libs.plugins.book.domain.api)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    api(projects.service.user.domain.api)
}
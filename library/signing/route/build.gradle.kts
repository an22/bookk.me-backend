plugins {
    alias(libs.plugins.bookk.domain.api)
}

dependencies {
    implementation(projects.library.signing.api)
    implementation(libs.ktor.core)
    implementation(libs.ktor.json)
    implementation(libs.koin.ktor)
    implementation(platform(libs.koin.bom))
}

plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.exposed.h2)
}
plugins {
    alias(libs.plugins.book.domain.api)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.domain)
}
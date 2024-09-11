plugins {
    alias(libs.plugins.book.data)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.data.cache.api)
    implementation(projects.service.user.domain.api)
}
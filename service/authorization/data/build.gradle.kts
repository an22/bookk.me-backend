plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.data.cache.api)
    implementation(projects.service.authorization.domain.api)
    implementation(projects.service.user.client)
}
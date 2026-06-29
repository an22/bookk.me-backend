plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.library.signing.impl)
    implementation(projects.core.data)
    implementation(projects.core.data.cache.api)
    implementation(projects.service.business.domain.api)
    implementation(projects.service.business.data.source)
}
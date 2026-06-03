plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.data)
    implementation(projects.core.data.cache.api)
    implementation(projects.service.appointments.domain.api)
    implementation(projects.service.appointments.data.source)
    implementation(libs.passkey)
}
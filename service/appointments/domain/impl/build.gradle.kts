plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.service.appointments.domain.api)
    implementation(projects.service.appointments.data.source)
    implementation(projects.library.permissions)
}
plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.data)
    implementation(projects.service.notifications.domain.api)
    implementation(projects.service.notifications.data.source)
}

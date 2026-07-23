plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.data)
    implementation(projects.service.notifications.domain.api)
    implementation(projects.service.notifications.data.source)

    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.core.data))
}

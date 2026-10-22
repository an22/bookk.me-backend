plugins {
    alias(libs.plugins.bookk.data)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.core.domain.datasource)

    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.core.data))
}

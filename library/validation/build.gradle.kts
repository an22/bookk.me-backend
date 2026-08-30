plugins {
    alias(libs.plugins.bookk.domain.api)
}

dependencies {
    testImplementation(testFixtures(projects.core))
}

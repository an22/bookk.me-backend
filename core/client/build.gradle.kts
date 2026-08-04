plugins {
    alias(libs.plugins.bookk.client)
}

dependencies {
    api(projects.core.domain)

    testImplementation(testFixtures(projects.core))
}

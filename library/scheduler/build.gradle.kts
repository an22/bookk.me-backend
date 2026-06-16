plugins {
    alias(libs.plugins.bookk.domain.api)
}

dependencies {
    implementation(projects.core)
    implementation(libs.ktor.core)
    testImplementation(testFixtures(projects.core))
    testImplementation(libs.kotlin.coroutines.test)
}

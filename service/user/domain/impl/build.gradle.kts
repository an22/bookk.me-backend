plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.jwt)
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.core.domain.datasource)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.service.user.domain.api)
    implementation(projects.service.user.data.source)
    implementation(projects.service.user.client)
    implementation(projects.service.authorization.client)
    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.core.domain.datasource))
    testImplementation(libs.kotlin.coroutines.test)
}
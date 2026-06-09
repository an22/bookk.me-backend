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
    implementation(projects.service.business.client)
    implementation(projects.service.appointments.client)
    implementation(projects.library.permissions)
    implementation(projects.library.money)
    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.core.domain.datasource))
    testImplementation(libs.joda.money)
    testImplementation(libs.kotlin.coroutines.test)
}
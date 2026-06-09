plugins {
    alias(libs.plugins.bookk.microservice)
}

group = "com.bookk.server.microservice.authorization"
version = "0.0.1"

application {
    mainClass.set("com.bookk.auth.AuthMicroserviceKt")
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.service)
    implementation(projects.core.domain)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.core.data.eventstreaming.impl)
    implementation(projects.core.data.cache.api)
    implementation(projects.core.data.cache.impl)
    implementation(projects.service.authorization.data)
    implementation(projects.service.authorization.domain.api)
    implementation(projects.service.authorization.domain.impl)
    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.core.service))
}
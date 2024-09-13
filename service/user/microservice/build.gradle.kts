plugins {
    alias(libs.plugins.book.microservice)
}

group = "com.bookk.server.microservice.user"
version = "0.0.1"

application {
    mainClass.set("com.book.user.UserMicroserviceKt")
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.service)
    implementation(projects.core.domain)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.core.data.eventstreaming.impl)
    implementation(projects.core.data.cache.api)
    implementation(projects.core.data.cache.impl)
    implementation(projects.service.user.data)
    implementation(projects.service.user.domain.api)
    implementation(projects.service.user.domain.impl)
}
plugins {
    alias(libs.plugins.bookk.microservice)
}

group = "com.bookk.server.microservice.business"
version = "0.0.1"

application {
    mainClass.set("com.book.business.BusinessMicroserviceKt")
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.service)
    implementation(projects.core.domain)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.core.data.eventstreaming.impl)
    implementation(projects.core.data.cache.api)
    implementation(projects.core.data.cache.impl)
    implementation(projects.service.business.data)
    implementation(projects.service.business.domain.api)
    implementation(projects.service.business.domain.impl)
}
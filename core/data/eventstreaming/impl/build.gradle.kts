plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.protobuf)
    implementation(libs.kafka.client)
    implementation(projects.core.data.eventstreaming.api)
    implementation(projects.core)
}
plugins {
    alias(libs.plugins.bookk.domain.impl)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.reactive)
    implementation(libs.ktor.protobuf)
    implementation(libs.redis.client)
    implementation(libs.redis.pool)
    implementation(projects.core)
    implementation(projects.core.data.cache.api)
}
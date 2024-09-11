plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.reactive)
    implementation(libs.ktor.protobuf)
    implementation(libs.redis.client)
    implementation(libs.redis.pool)
    implementation(projects.core.data.cache.api)
}
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin.sourceSets.all {
    languageSettings.optIn("io.lettuce.core.ExperimentalLettuceCoroutinesApi")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.reactive)
    implementation(libs.ktor.protobuf)
    implementation(libs.redis.client)
    implementation(libs.redis.pool)
    implementation(projects.core.data.cache.api)
}
import com.bookk.build_src.tools.libs

plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.kotlin.fixtures.get().pluginId)
}

dependencies {
    api(libs.kotlin.datetime)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.std)
    testImplementation(libs.kotlin.test)
    testFixturesImplementation(libs.mockk)
    testFixturesApi(libs.kotlin.coroutines.test)
}
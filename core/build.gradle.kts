import com.bookk.build_src.tools.libs

plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.kotlin.fixtures.get().pluginId)
}

dependencies {
    implementation(libs.kotlin.coroutines)
    testImplementation(libs.kotlin.test)
}
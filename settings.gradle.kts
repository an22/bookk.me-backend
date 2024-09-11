enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-src")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "bookk-server"

//Core
include(":core")
include(":core:domain")
include(":core:data")
include(":core:data:cache:api")
include(":core:data:cache:impl")
include(":core:data:eventstreaming:api")
include(":core:data:eventstreaming:impl")
include(":core:service")

//Authorization
include(":service:authorization:microservice")
include(":service:authorization:domain:api")
include(":service:authorization:domain:impl")
include(":service:authorization:data")
include(":service:authorization:client")

//User
include(":service:user:microservice")
include(":service:user:domain:api")
include(":service:user:domain:impl")
include(":service:user:data")
include(":service:user:client")


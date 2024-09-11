plugins {
    `kotlin-dsl`
}

group = "com.bookk"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.ktor.plugin)
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("microservice") {
            id = "com.bookk.microservice"
            implementationClass = "com.bookk.MicroserviceConventionPlugin"
            version = "1.0"
        }
        register("data") {
            id = "com.bookk.data"
            implementationClass = "com.bookk.DataConventionPlugin"
            version = "1.0"
        }
        register("domainApi") {
            id = "com.bookk.domain.api"
            implementationClass = "com.bookk.DomainApiConventionPlugin"
            version = "1.0"
        }
        register("domainImpl") {
            id = "com.bookk.domain.impl"
            implementationClass = "com.bookk.DomainImplConventionPlugin"
            version = "1.0"
        }
    }
}
package com.bookk.core

object AppLevelConstants {
    const val DOMAIN_NAME = "bookkme.app"
    const val APP_NAME = "BookkMe"
    const val SERIALIZER = SupportedSerializers.PROTOBUF.STR
    const val BUILD_TYPE = BuildType.DEBUG.STR

    val serviceName: String
        get() = System.getenv("APPLICATION_SERVICE_NAME").orEmpty()
    val serviceVersion: String
        get() = System.getenv("APPLICATION_SERVICE_VERSION").orEmpty()
    val cacheHost: String
        get() = System.getenv("APPLICATION_REDIS_HOSTS")
    val cachePort: String
        get() = System.getenv("APPLICATION_REDIS_PORT")
    val cachePass: String
        get() = System.getenv("APPLICATION_REDIS_PASSWORD")
    val eventStreamingHost: String
        get() = System.getenv("APPLICATION_KAFKA_HOSTS")
    val sslFile: String
        get() = System.getenv("APPLICATION_SERVICE_SSL_FILE")
    val sslPass: String
        get() = System.getenv("APPLICATION_SERVICE_SSL_PASSWORD")
    val sslAlias: String
        get() = System.getenv("APPLICATION_SERVICE_SSL_ALIAS")
    val sslPort: Int
        get() = System.getenv("APPLICATION_SERVICE_PORT").toInt()
    val dbSchemaName: String
        get() = System.getenv("APPLICATION_DB_SCHEME")
    val dbUrl: String
        get() = System.getenv("APPLICATION_DB_URL")
    val dbPort: String
        get() = System.getenv("APPLICATION_DB_PORT")
    val dbUsername: String
        get() = System.getenv("APPLICATION_DB_USER")
    val dbPassword: String
        get() = System.getenv("APPLICATION_DB_PASSWORD")
    val pubKeyFilename: String
        get() = System.getenv("APPLICATION_JWT_PUBLIC_KEY_FILE")
    val privateKeyFilename: String
        get() = System.getenv("APPLICATION_JWT_PRIVATE_KEY_FILE")

    sealed interface SupportedSerializers {
        data object JSON : SupportedSerializers {
            const val STR = "JSON"
        }

        data object PROTOBUF : SupportedSerializers {
            const val STR = "PROTOBUF"
        }
    }

    sealed interface BuildType {
        data object DEBUG : SupportedSerializers {
            const val STR = "DEBUG"
        }

        data object RELEASE : SupportedSerializers {
            const val STR = "RELEASE"
        }
    }

    enum class Claim(val key: String) {
        AUTH_ID("auth_id"),
        USER_ID("user_id"),
        DEVICE_ID("device_id")
    }
}
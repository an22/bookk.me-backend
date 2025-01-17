package com.bookk.core

object AppLevelConstants {
    const val DOMAIN_NAME = "bookkk.me"
    const val APP_NAME = "BookkMe"
    const val SERIALIZER = SupportedSerializers.PROTOBUF.STR
    const val BUILD_TYPE = BuildType.RELEASE.STR

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
        IS_REFRESH("refresh"),
        AUTH_ID("auth_id"),
        USER_ID("user_id"),
        DEVICE_ID("device_id")
    }
}
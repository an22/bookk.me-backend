package com.bookk.core

object AppLevelConstants {
    const val DOMAIN_NAME = "bookkk.me"
    const val APP_NAME = "BookkMe"
    const val SERIALIZER = SupportedSerializers.PROTOBUF.STR
    const val BUILD_TYPE = BuildType.RELEASE.STR
    const val EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\\\])"

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
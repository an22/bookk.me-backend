package com.bookk.core

object AppLevelConstants {
    const val DOMAIN_NAME = "bookkk.me"
    const val APP_NAME = "BookkMe"
    val BUILD_TYPE = BuildType.RELEASE

    enum class BuildType {
        DEBUG,
        RELEASE
    }
}
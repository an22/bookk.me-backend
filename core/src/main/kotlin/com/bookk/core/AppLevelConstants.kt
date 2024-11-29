package com.bookk.core

object AppLevelConstants {
    const val DOMAIN_NAME = "bookkk.me"
    val BUILD_TYPE = BuildType.DEBUG

    enum class BuildType {
        DEBUG,
        RELEASE
    }
}
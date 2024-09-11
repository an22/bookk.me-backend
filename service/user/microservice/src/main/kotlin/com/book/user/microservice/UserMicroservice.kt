package com.book.user.microservice

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.RedisCacheClient
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import com.book.user.data.di.userDataModule
import com.book.user.data.di.userQualifier
import com.book.user.domain.impl.di.userDomainModule
import com.book.user.microservice.route.userRoute
import io.ktor.server.routing.*
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun userModule() = module {
    includes(
        userDataModule(),
        userDomainModule()
    )
    single<CacheClient<String>>(userQualifier) {
        RedisCacheClient(
            host = System.getenv("me.bookk.redis_host"),
            port = System.getenv("me.bookk.redis_port").toInt(),
            protobuf = ProtoBuf { encodeDefaults = true }
        )
    }
}

fun main() {
    startServer(diModules = listOf(userModule())) {
        routing {
            installNegotiation()
            userRoute()
        }
    }
}
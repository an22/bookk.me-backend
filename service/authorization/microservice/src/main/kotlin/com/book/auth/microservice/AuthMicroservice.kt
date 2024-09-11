package com.book.auth.microservice

import com.book.auth.data.di.authDataModule
import com.book.auth.data.di.authQualifier
import com.book.auth.domain.impl.di.DIQualifier
import com.book.auth.domain.impl.di.authDomainModule
import com.book.auth.microservice.route.authRoute
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.RedisCacheClient
import com.book.core.service.auth.JwtConfig
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import io.ktor.server.routing.*
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun authModule() = module {
    single(DIQualifier.DOMAIN_NAME) { System.getenv(DIQualifier.DOMAIN_NAME.value) }
    single<CacheClient<String>>(authQualifier) {
        RedisCacheClient(
            host = System.getenv("me.bookk.redis_host"),
            port = System.getenv("me.bookk.redis_port").toInt(),
            protobuf = ProtoBuf { encodeDefaults = true }
        )
    }
    single { JwtConfig.createPrivateKeyProvider() }
    includes(
        authDataModule(),
        authDomainModule()
    )
}

fun main() {
    startServer(diModules = listOf(authModule())) {
        routing {
            installNegotiation()
            authRoute()
        }
    }
}

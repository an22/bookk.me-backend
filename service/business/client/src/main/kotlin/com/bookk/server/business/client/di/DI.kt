package com.bookk.server.business.client.di

import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.SupportedSerializers
import com.bookk.server.business.client.api.BusinessClient
import com.bookk.server.business.client.impl.BusinessClientImpl
import com.bookk.server.business.client.impl.operation.GetBusinessByIdClientImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.uuid.Uuid

private const val BUSINESS_HTTP_CLIENT = "businessHttpClient"

@Suppress("KotlinConstantConditions")
fun businessClientModule(clientTag: String) = module {
    single(named(BUSINESS_HTTP_CLIENT)) {
        HttpClient(CIO) {
            install(Resources)
            install(UserAgent) { agent = clientTag }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = 3)
                constantDelay(millis = 50, randomizationMs = 100)
            }
            install(ContentNegotiation) {
                when (AppLevelConstants.SERIALIZER) {
                    SupportedSerializers.JSON.STR -> {
                        json(Json {
                            prettyPrint = true
                            encodeDefaults = true
                            explicitNulls = false
                        })
                    }

                    SupportedSerializers.PROTOBUF.STR -> {
                        protobuf(ProtoBuf { encodeDefaults = true })
                    }
                }
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = when (AppLevelConstants.BUILD_TYPE) {
                    AppLevelConstants.BuildType.DEBUG.STR -> LogLevel.BODY
                    else -> LogLevel.INFO
                }
            }
            defaultRequest {
                host = System.getenv("APPLICATION_BUSINESS_SERVICE_HOSTNAME")

                headers["Idempotency-Key"] = Uuid.random().toString()

                url { protocol = URLProtocol.HTTP }

                when (AppLevelConstants.SERIALIZER) {
                    SupportedSerializers.JSON.STR -> contentType(ContentType.Application.Json)
                    SupportedSerializers.PROTOBUF.STR -> contentType(ContentType.Application.ProtoBuf)
                }
            }
        }
    }
    single<GetBusinessById> { GetBusinessByIdClientImpl(get(named(BUSINESS_HTTP_CLIENT))) }
    single<BusinessClient> { BusinessClientImpl(get()) }
}

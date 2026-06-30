package com.bookk.core.service

import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.SupportedSerializers
import com.bookk.core.service.di.commonModule
import com.bookk.server.auth.client.authTokenValidator
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.cio.CIO
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import library.idempotency.IdempotencyPlugin
import library.signing.TokenValidatorFactory
import library.signing.ValidationType
import org.koin.core.module.Module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level

fun startServer(
    diModules: List<Module> = emptyList(),
    config: ServiceConfig = ServiceConfig(),
    modules: Routing.(Application) -> Unit
) {
    embeddedServer(
        factory = CIO,
        configure = {
            connector {
                port = AppLevelConstants.servicePort
            }
        },
        module = {
            val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            install(MicrometerMetrics) {
                registry = prometheusRegistry
            }
            install(Koin) { modules(*diModules.toTypedArray(), commonModule()) }
            install(CallLogging) {
                level = when (AppLevelConstants.BUILD_TYPE) {
                    AppLevelConstants.BuildType.DEBUG.STR -> Level.DEBUG
                    else -> Level.INFO
                }
                format { call ->
                    val status = call.response.status()
                    val httpMethod = call.request.httpMethod.value
                    val path = call.request.path()
                    "[$status], $httpMethod $path"
                }
            }
            install(Resources)
            install(IdempotencyPlugin) {
                idempotentResponseRepository = get()
                cleanUpWorkerEnabled = false //We use Redis expiration time for cache management
            }
            installAuthPlugin()
            routing {
                get("/metrics") {
                    call.respond(prometheusRegistry.scrape())
                }
                val contact = OpenApiInfo.Contact(
                    name = "Michael Antiufieiev",
                    email = "antufeevmichael@gmail.com"
                )
                val apiInfo = OpenApiInfo(
                    config.title,
                    config.version,
                    contact = contact
                )
                swaggerUI("/${config.root}/internal/swagger") {
                    info = apiInfo
                    source = OpenApiDocSource.Routing {
                        routingRoot.descendants()
                    }
                }
                openAPI(path = "/${config.root}/internal/openapi") {
                    info = apiInfo
                    source = OpenApiDocSource.Routing {
                        routingRoot.descendants()
                    }
                }
                modules(this@embeddedServer)
            }
        }
    ).start(wait = true)
}

private fun Application.installAuthPlugin() {
    val validatorFactory: TokenValidatorFactory = get()
    install(Authentication) {
        jwt {
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
            verifier(validatorFactory.forType(ValidationType.AUTH_TOKEN).verifier)
            validate(authTokenValidator)
        }
    }
}

@Suppress("KotlinConstantConditions")
fun Routing.installNegotiation() {
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
}
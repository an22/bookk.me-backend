package com.bookk.core.service

import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.SupportedSerializers
import com.bookk.core.service.auth.AccessVerifier
import com.bookk.core.service.auth.RefreshVerifier
import com.bookk.core.service.di.commonModule
import com.wolt.utils.ktor.idempotency.IdempotencyPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.module.Module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level
import java.io.File
import java.security.KeyStore

fun startServer(
    diModules: List<Module> = emptyList(),
    modules: Routing.(Application) -> Unit
) {
    embeddedServer(
        factory = Netty,
        configure = {
            val keystoreFile = File(AppLevelConstants.sslFile)
            val keyStorePass = AppLevelConstants.sslPass
            sslConnector(
                keyStore = KeyStore.getInstance(
                    keystoreFile,
                    keyStorePass.toCharArray()
                ),
                keyAlias = AppLevelConstants.sslAlias,
                keyStorePassword = { keyStorePass.toCharArray() },
                privateKeyPassword = { keyStorePass.toCharArray() }
            ) {
                port = AppLevelConstants.sslPort.toInt()
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
                swaggerUI("/docs", swaggerFile = "openapi/generated.json")
                modules(this@embeddedServer)
            }
        }
    ).start(wait = true)
}

private fun Application.installAuthPlugin() {
    install(Authentication) {
        jwt {
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
            verifier(AccessVerifier.verifier)
            validate(AccessVerifier.validator)
        }
        jwt("refresh") {
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
            verifier(RefreshVerifier.verifier)
            validate(RefreshVerifier.validator)
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
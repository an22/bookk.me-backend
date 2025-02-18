package com.book.core.service

import com.book.core.service.auth.AccessVerifier
import com.book.core.service.auth.RefreshVerifier
import com.book.core.service.di.commonModule
import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.SupportedSerializers
import com.wolt.utils.ktor.idempotency.IdempotencyPlugin
import io.bkbn.kompendium.core.attribute.KompendiumAttributes
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.core.routes.swagger
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Contact
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.module.Module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level
import java.io.File
import java.security.KeyStore

@Suppress("KotlinConstantConditions")
fun startServer(
    diModules: List<Module> = emptyList(),
    modules: Application.() -> Unit
) {
    embeddedServer(
        factory = Netty,
        configure = {
            val keystoreFile = File(System.getenv("BOOKK_ME_SERVICE_SSL_FILE"))
            val keyStorePass = System.getenv("BOOKK_ME_SERVICE_SSL_PASSWORD")
            sslConnector(
                keyStore = KeyStore.getInstance(
                    keystoreFile,
                    keyStorePass.toCharArray()
                ),
                keyAlias = System.getenv("BOOKK_ME_SERVICE_SSL_ALIAS"),
                keyStorePassword = { keyStorePass.toCharArray() },
                privateKeyPassword = { keyStorePass.toCharArray() }
            ) {
                port = System.getenv("BOOKK_ME_SERVICE_PORT").toInt()
            }
        },
        module = {
            install(Koin) { modules(*diModules.toTypedArray(), commonModule()) }
            install(CallLogging) {
                level = when(AppLevelConstants.BUILD_TYPE) {
                    AppLevelConstants.BuildType.DEBUG.STR -> Level.DEBUG
                    else -> Level.INFO
                }
            }
            install(Resources)
            install(IdempotencyPlugin) {
                idempotentResponseRepository = get()
                //We use Redis expiration time for cache management
                cleanUpWorkerEnabled = false
            }
            installAuthPlugin()
            installDocumentationPlugin()
            modules()
        }
    ).start(wait = true)
}

private fun Application.installAuthPlugin() {
    install(Authentication) {
        jwt {
            verifier(AccessVerifier.verifier)
            validate(AccessVerifier.validator)
        }
        jwt("refresh") {
            verifier(RefreshVerifier.verifier)
            validate(RefreshVerifier.validator)
        }
    }
}

fun Application.installDocumentationPlugin() {
    install(NotarizedApplication()) {
        spec = {
            OpenApiSpec(
                jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base",
                info = Info(
                    title = "BookkMeDocs",
                    version = "0.0.1",
                    description = "BookkMe documentation",
                    contact = Contact("Michael Antiufieiev", email = "antufeevmichael@gmail.com")
                ),
                components = Components(
                    securitySchemes = mutableMapOf(
                        "jwt" to BearerAuth("JWT")
                    )
                )
            )
        }
        specRoute = { _: OpenApiSpec, _: Routing ->
            routing {
                route("/api/${System.getenv("BOOKK_ME_SERVICE_NAME")}/openapi.json") {
                    install(ContentNegotiation) {
                        json(Json {
                            serializersModule = KompendiumSerializersModule.module
                            encodeDefaults = true
                            explicitNulls = false
                        })
                    }
                    get {
                        call.respond(
                            HttpStatusCode.OK,
                            this@route.application.attributes[KompendiumAttributes.openApiSpec]
                        )
                    }
                }
                redoc(
                    path = "/api/${System.getenv("BOOKK_ME_SERVICE_NAME")}/redoc",
                    specUrl = "/api/${System.getenv("BOOKK_ME_SERVICE_NAME")}/openapi.json"
                )
                swagger(
                    path = "/api/${System.getenv("BOOKK_ME_SERVICE_NAME")}/swagger",
                    specUrl = "/api/${System.getenv("BOOKK_ME_SERVICE_NAME")}/openapi.json"
                )
            }
        }
        schemaConfigurator = KotlinXSchemaConfigurator()
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

@Suppress("KotlinConstantConditions")
fun ResponseInfo.Builder.applyMediaType() {
    when (AppLevelConstants.SERIALIZER) {
        SupportedSerializers.JSON.STR -> mediaTypes(ContentType.Application.Json.toString())
        SupportedSerializers.PROTOBUF.STR -> mediaTypes(ContentType.Application.ProtoBuf.toString())
    }
}

@Suppress("KotlinConstantConditions")
fun RequestInfo.Builder.applyMediaType() {
    when (AppLevelConstants.SERIALIZER) {
        SupportedSerializers.JSON.STR -> mediaTypes(ContentType.Application.Json.toString())
        SupportedSerializers.PROTOBUF.STR -> mediaTypes(ContentType.Application.ProtoBuf.toString())
    }
}
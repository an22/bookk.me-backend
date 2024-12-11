package com.book.core.service

import com.book.core.service.auth.JwtConfig
import com.bookk.core.AppLevelConstants
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
import org.koin.ktor.plugin.Koin
import java.security.KeyStore

fun startServer(
    diModules: List<Module> = emptyList(),
    modules: Application.() -> Unit
) {
    embeddedServer(
        factory = Netty,
        configure = {
            val keystoreStream = javaClass.classLoader.getResourceAsStream(System.getenv("BOOKK_ME_SERVICE_SSL_FILE"))
            val keyStorePass = System.getenv("BOOKK_ME_SERVICE_SSL_PASSWORD")
            sslConnector(
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    load(keystoreStream, keyStorePass.toCharArray())
                },
                keyAlias = System.getenv("BOOKK_ME_SERVICE_SSL_ALIAS"),
                keyStorePassword = { keyStorePass.toCharArray() },
                privateKeyPassword = { keyStorePass.toCharArray() }
            ) {
                port = System.getenv("BOOKK_ME_SERVICE_PORT").toInt()
            }
        },
        module = {
            install(Koin) { modules(diModules) }
            install(CallLogging)
            install(Resources)
            installAuthPlugin()
            installDocumentationPlugin()
            modules()
        }
    ).start(wait = true)
}

private fun Application.installAuthPlugin() {
    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            validate(JwtConfig.validator)
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
                route("/openapi.json") {
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
                redoc()
                swagger()
            }
        }
        schemaConfigurator = KotlinXSchemaConfigurator()
    }
}

fun Routing.installNegotiation() {
    install(ContentNegotiation) {
        when (AppLevelConstants.BUILD_TYPE) {
            AppLevelConstants.BuildType.DEBUG -> {
                json(Json {
                    prettyPrint = true
                    encodeDefaults = true
                    explicitNulls = false
                })
            }

            AppLevelConstants.BuildType.RELEASE -> {
                protobuf(ProtoBuf { encodeDefaults = true })
            }
        }
    }
}

fun ResponseInfo.Builder.applyMediaType() {
    when (AppLevelConstants.BUILD_TYPE) {
        AppLevelConstants.BuildType.DEBUG -> mediaTypes(ContentType.Application.Json.toString())
        AppLevelConstants.BuildType.RELEASE -> mediaTypes(ContentType.Application.ProtoBuf.toString())
    }
}

fun RequestInfo.Builder.applyMediaType() {
    when (AppLevelConstants.BUILD_TYPE) {
        AppLevelConstants.BuildType.DEBUG -> mediaTypes(ContentType.Application.Json.toString())
        AppLevelConstants.BuildType.RELEASE -> mediaTypes(ContentType.Application.ProtoBuf.toString())
    }
}
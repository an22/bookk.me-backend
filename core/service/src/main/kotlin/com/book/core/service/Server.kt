package com.book.core.service

import com.book.core.service.auth.JwtConfig
import io.bkbn.kompendium.core.attribute.KompendiumAttributes
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
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import java.io.File
import java.security.KeyStore

@OptIn(ExperimentalSerializationApi::class)
fun startServer(
    diModules: List<Module> = emptyList(),
    modules: Application.() -> Unit
) {
    embeddedServer(
        factory = Netty,
        environment = applicationEngineEnvironment {
            developmentMode = true

            val keyStoreFile = File(System.getenv("me.bookk.ssl_filename"))
            val keyStorePass = System.getenv("me.bookk.ssl_pass")
            sslConnector(
                keyStore = KeyStore.getInstance(keyStoreFile, keyStorePass.toCharArray()),
                keyAlias = System.getenv("me.bookk.ssl_alias"),
                keyStorePassword = { keyStorePass.toCharArray() },
                privateKeyPassword = { keyStorePass.toCharArray() }
            ) {
                port = System.getenv("me.bookk.port").toInt()
                keyStorePath = keyStoreFile
            }
            module {
                install(Koin) {
                    modules(diModules)
                }
                install(Authentication) {
                    jwt {
                        verifier(JwtConfig.verifier)
                        validate(JwtConfig.validator)
                    }
                }
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
                    specRoute = { _: OpenApiSpec, routing: Routing ->
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
                modules()
            }
        }
    ).start(wait = true)
}

fun Routing.installNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            explicitNulls = false
        })
    }
}
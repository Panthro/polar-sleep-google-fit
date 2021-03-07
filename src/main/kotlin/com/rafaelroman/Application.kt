package com.rafaelroman

import com.rafaelroman.application.polarauthentication.AuthorizeWithPolarUseCase
import com.rafaelroman.application.syncpolarsleepdata.SyncPolarSleepDataUseCase
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import com.rafaelroman.infrastructure.clients.HttpPolarClient
import com.rafaelroman.infrastructure.persistence.ExposedPolarAccessTokenRepository
import io.ktor.routing.*
import io.ktor.locations.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.html.respondHtml
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.title

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)


/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    val polarClientId = environment.config.property("polar.oauth2.clientId").getString()
    val polarClientSecret = environment.config.property("polar.oauth2.clientSecret").getString()

    val googleClientId = environment.config.property("google.oauth2.clientId").getString()
    val googleClientSecret = environment.config.property("google.oauth2.clientSecret").getString()

    val polarHttpClient = HttpPolarClient(
        client,
        clientId = polarClientId,
        clientSecret = polarClientSecret
    )
    val polarAccessTokenRepository: PolarAccessTokenRepository = ExposedPolarAccessTokenRepository()

    val authorizeWithPolarUseCase = AuthorizeWithPolarUseCase(polarHttpClient, polarAccessTokenRepository)

    val syncPolarSleepDataUseCase = SyncPolarSleepDataUseCase(polarAccessTokenRepository, polarHttpClient)


    install(Locations) {
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
//    install(ContentNegotiation) {
//        json()
//    }


    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Polar sleep sync")
                }
                body {
                    div {
                        a {
                            href = "https://flow.polar.com/oauth2/authorization?response_type=code&client_id=$polarClientId"
                            span { +"Connect polar" }
                        }
                        br { }
                        a {
                            href = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
                                    "redirect_uri=http://localhost:8080/callback/google" +
                                    "&prompt=consent" +
                                    "&response_type=code" +
                                    "&client_id=$googleClientId" +
                                    "&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Ffitness.sleep.write" +
                                    "&access_type=offline" +
                                    "&flowName=GeneralOAuthFlow"
                            span { +"Connect Google" }
                        }
                    }
                }
            }
        }
    }
    routing {
        get<PolarAuthenticationCallback> { callback ->
            authorizeWithPolarUseCase authorize PolarAuthorizationRequestCode(callback.code)
            call.respondText("Polar connected")
        }
        get<GoogleAuthenticationCallback> { callback ->

            call.respondText("Google code ${callback.code}")
        }

        get<PolarSleepRequest> {
            syncPolarSleepDataUseCase.sync()
            call.respondText { "Sync successfully" }
        }
    }
}

@Location("/callback/polar")
class PolarAuthenticationCallback(val code: String)

@Location("/callback/google")
class GoogleAuthenticationCallback(val code: String)

@Location("/sync/sleep")
class PolarSleepRequest()



package com.rafaelroman

import com.rafaelroman.application.googleauth.AuthorizeWithGoogleUseCase
import com.rafaelroman.application.polarauthentication.AuthorizeWithPolarUseCase
import com.rafaelroman.application.syncpolarsleepdata.SyncPolarSleepDataUseCase
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import com.rafaelroman.infrastructure.clients.google.GoogleHttpClient
import com.rafaelroman.infrastructure.clients.polar.HttpPolarClient
import com.rafaelroman.infrastructure.persistence.ExposedGoogleAccessTokenRepository
import com.rafaelroman.infrastructure.persistence.ExposedPolarAccessTokenRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.config.ApplicationConfig
import io.ktor.features.CallLogging
import io.ktor.html.respondHtml
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.locations.href
import io.ktor.request.path
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.span
import kotlinx.html.title
import org.jetbrains.exposed.sql.Database
import org.slf4j.event.Level

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

const val DATABASE_URL = "jdbc:h2:file:./db"
const val DATABASE_DRIVER = "org.h2.Driver"

fun ApplicationConfig.orDefault(path: String, default: String): String = propertyOrNull(path)?.getString() ?: default
fun ApplicationConfig.required(path: String): String = propertyOrNull(path)!!.getString()

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client by lazy {
        HttpClient(CIO) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }

    val db by lazy {
        Database.connect(
            url = environment.config.orDefault("db.url", DATABASE_URL),
            driver = environment.config.orDefault("db.driver", DATABASE_DRIVER)
        )
    }

    val polarClientId = environment.config.required("polar.oauth2.clientId")
    val polarClientSecret = environment.config.required("polar.oauth2.clientSecret")

    val googleClientId = environment.config.required("google.oauth2.clientId")
    val googleClientSecret = environment.config.required("google.oauth2.clientSecret")

    val redirectBase = environment.config.required("redirectBase")

    val polarHttpClient = HttpPolarClient(
        client,
        clientId = polarClientId,
        clientSecret = polarClientSecret
    )
    val googleHttpClient = GoogleHttpClient(
        client,
        clientId = googleClientId,
        clientSecret = googleClientSecret,
        redirectBase = redirectBase
    )
    val polarAccessTokenRepository: PolarAccessTokenRepository = ExposedPolarAccessTokenRepository(db)
    val googleAccessTokenRepository: GoogleAccessTokenRepository = ExposedGoogleAccessTokenRepository(db)

    val authorizeWithPolarUseCase = AuthorizeWithPolarUseCase(polarHttpClient, polarAccessTokenRepository)
    val authorizeWithGoogleUseCase = AuthorizeWithGoogleUseCase(googleHttpClient, googleAccessTokenRepository)

    val syncPolarSleepDataUseCase = SyncPolarSleepDataUseCase(polarAccessTokenRepository, polarHttpClient, googleHttpClient, googleAccessTokenRepository)

    install(Locations) {
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Polar sleep sync")
                }
                body {
                    div {

                        div {
                            a {
                                href = "https://flow.polar.com/oauth2/authorization?response_type=code&client_id=$polarClientId"
                                span { +"Connect polar" }
                            }
                        }
                    }
                }
            }
        }
    }
    routing {
        get<PolarConnected> { polar ->
            run {
                when (polarAccessTokenRepository.find(polar.userId)) {
                    null -> {
                        call.respondRedirect("/")
                    }
                    else -> {
                        call.respondHtml {
                            head {
                                title("Polar sleep sync")
                            }
                            body {
                                span { +"Polar Connected" }
                                br { }
                                a {
                                    href = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
                                            "redirect_uri=$redirectBase/callback/google" +
                                            "&state=${polar.userId}" +
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
        }
        get<PolarAuthenticationCallbackLocation> { callback ->
            val accessToken = authorizeWithPolarUseCase authorize PolarAuthorizationRequestCode(callback.code)
            call.respondRedirect(
                href(PolarConnected(accessToken.userId))
            )
        }
        get<GoogleAuthenticationCallbackLocation> { callback ->
            authorizeWithGoogleUseCase authorize GoogleAuthorizationRequestCode(callback.code, callback.state)
            call.respondRedirect(
                href(SyncSleepLocation(callback.state))
            )
        }

        get<SyncSleepLocation> { sync ->
            syncPolarSleepDataUseCase.sync(sync.userId)
            call.respondText { "Sync successfully" }
        }
    }
}

@Location("/callback/polar")
class PolarAuthenticationCallbackLocation(val code: String)

@Location("/callback/google")
class GoogleAuthenticationCallbackLocation(val code: String, val state: String)

@Location("/sync/sleep/{userId}")
class SyncSleepLocation(val userId: String)

@Location("/polar/{userId}")
class PolarConnected(val userId: String)

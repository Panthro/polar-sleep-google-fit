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
import io.ktor.response.*
import io.ktor.request.*

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
    val polarHttpClient = HttpPolarClient(
        client,
        clientId = environment.config.property("polar.oauth2.clientId").getString(),
        clientSecret = environment.config.property("polar.oauth2.clientSecret").getString()
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
            call.respondText("Hello World!")
        }
    }
    routing {
        get<PolarAuthenticationCallback> { callback ->
            authorizeWithPolarUseCase authorize PolarAuthorizationRequestCode(callback.code)
            call.respondText("Polar connected")
        }

        get<PolarSleepRequest> {
            syncPolarSleepDataUseCase.sync()
            call.respondText { "Sync successfully" }
        }
    }
}

@Location("/callback")
class PolarAuthenticationCallback(val code: String)

@Location("/sync/sleep")
class PolarSleepRequest()



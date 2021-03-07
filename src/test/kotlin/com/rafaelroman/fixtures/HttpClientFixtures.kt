package com.rafaelroman.fixtures

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.matchesPredicate
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.http.hostWithPort
import java.util.Base64

fun mockHttpClient(handler: MockRequestHandler) = HttpClient(MockEngine) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    engine {
        addHandler(handler)
    }
}


val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"

fun mockPolarNightsRequest(accessToken: String): MockRequestHandler = { request ->
    when (request.url.fullUrl) {
        "https://www.polaraccesslink.com/v3/users/sleep" -> {

            assertThat(request.headers["Authorization"]).isEqualTo("Bearer $accessToken")

            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            respond("""
                        {
                          "nights": [
                            {
                              "polar_user": "https://www.polaraccesslink/v3/users/1",
                              "date": "2020-01-01",
                              "sleep_start_time": "2020-01-01T00:39:07+03:00",
                              "sleep_end_time": "2020-01-01T09:19:37+03:00",
                              "device_id": "1111AAAA",
                              "continuity": 2.1,
                              "continuity_class": 2,
                              "light_sleep": 1000,
                              "deep_sleep": 1000,
                              "rem_sleep": 1000,
                              "unrecognized_sleep_stage": 1000,
                              "sleep_score": 80,
                              "total_interruption_duration": 1000,
                              "sleep_charge": 3,
                              "sleep_goal": 28800,
                              "sleep_rating": 3,
                              "short_interruption_duration": 500,
                              "long_interruption_duration": 300,
                              "sleep_cycles": 6,
                              "group_duration_score": 100,
                              "group_solidity_score": 75,
                              "group_regeneration_score": 54.2,
                              "hypnogram": {
                                "00:39": 2,
                                "00:50": 3,
                                "01:23": 6
                              },
                              "heart_rate_samples": {
                                "00:41": 76,
                                "00:46": 77,
                                "00:51": 76
                              }
                            }
                          ]
                        }
                    """.trimIndent(), headers = responseHeaders)
        }
        else -> error("Unhandled ${request.url.fullUrl}")

    }
}

fun mockPolarAccessTokenRequest(
    code: String,
    accessToken: String,
    expiresIn: Long,
    userId: Long,
    clientId: String,
    clientSecret: String,
): MockRequestHandler = { request ->
    when (request.url.fullUrl) {
        "https://polarremote.com/v2/oauth2/token" -> {

            assertThat(request.method).isEqualTo(HttpMethod.Post)
            assertThat(request.body.contentType.toString()).isEqualTo("application/x-www-form-urlencoded; charset=UTF-8")
            assertThat(request.headers["Authorization"]).isEqualTo("Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
            assertThat(request.body).isInstanceOf(FormDataContent::class)
            assertThat((request.body as FormDataContent).formData).isEqualTo(Parameters.build {
                append("code", code)
                append("grant_type", "authorization_code")
            })

            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            respond("""
                        {
                            "access_token": "$accessToken",
                            "token_type": "bearer",
                            "expires_in": $expiresIn,
                            "x_user_id": $userId
                        }
                    """.trimIndent(), headers = responseHeaders)
        }
        "https://www.polaraccesslink.com/v3/users" -> {
            assertThat(request.method).isEqualTo(HttpMethod.Post)
            assertThat(request.body.contentType).isEqualTo(ContentType.Application.Json)
            assertThat(request.headers["Authorization"]).matchesPredicate { it!!.startsWith("Bearer ") }
            assertThat((request.body as TextContent).text).isEqualTo("{\"member-id\":\"${userId}\"}")
            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))

            respond("""
                        {
                          "polar-user-id": 2278512,
                          "member-id": "i09u9ujj",
                          "registration-date": "2011-10-14T12:50:37.000Z",
                          "first-name": "Eka",
                          "last-name": "Toka",
                          "birthdate": "1985-09-06T00:00:00.000Z",
                          "gender": "MALE",
                          "weight": 66,
                          "height": 170,
                          "field": [
                            {
                              "value": "2",
                              "index": 0,
                              "name": "number-of-children"
                            }
                          ]
                        }
                    """.trimIndent(), headers = responseHeaders)

        }
        else -> error("Unhandled ${request.url.fullUrl}")
    }
}


fun mockGoogleAccessTokenRequest(
    code: String,
    accessToken: String,
    expiresIn: Long,
    refreshToken: String,
    clientId: String,
    clientSecret: String,
): MockRequestHandler = { request ->
    when (request.url.fullUrl) {
        "https://oauth2.googleapis.com/token" -> {

            assertThat(request.method).isEqualTo(HttpMethod.Post)
            assertThat(request.body.contentType.toString()).isEqualTo("application/x-www-form-urlencoded; charset=UTF-8")
            assertThat(request.body).isInstanceOf(FormDataContent::class)
            assertThat((request.body as FormDataContent).formData).isEqualTo(Parameters.build {
                append("code", code)
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("grant_type", "authorization_code")
                append("redirect_uri", "http://localhost:8080/callback/google")
            })

            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            respond("""
                        {
                            "access_token": "$accessToken",
                            "token_type": "Bearer", 
                            "expires_in": $expiresIn,
                            "scope": "https://www.googleapis.com/auth/fitness.sleep.write",
                             "refresh_token": "$refreshToken"
                        }
                    """.trimIndent(), headers = responseHeaders)
        }
        else -> error("Unhandled ${request.url.fullUrl}")
    }
}


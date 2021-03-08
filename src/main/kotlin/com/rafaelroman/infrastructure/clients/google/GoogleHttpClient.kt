package com.rafaelroman.infrastructure.clients.google

import com.google.gson.annotations.SerializedName
import com.rafaelroman.domain.googlefit.GoogleAccessToken
import com.rafaelroman.domain.googlefit.GoogleAccessTokenProvider
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublished
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublisher
import com.rafaelroman.domain.sleep.SleepNight
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType

class GoogleHttpClient(
    private val client: HttpClient,
    private val clientSecret: String,
    private val clientId: String,
    private val idProvider: SleepNightIdentifierProvider = SleepNightIdentifierProvider,
) : GoogleAccessTokenProvider, GoogleFitSleepNightPublisher {
    override suspend fun withCode(googleAuthorizationRequestCode: GoogleAuthorizationRequestCode): GoogleAccessToken =
        client.submitForm<GoogleAccessTokenHttpResponse>("https://oauth2.googleapis.com/token") {
            accept(ContentType.Application.Json)
            body = FormDataContent(
                Parameters.build {
                    append("code", googleAuthorizationRequestCode.code)
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", "http://localhost:8080/callback/google")
                }
            )
        }.toGoogleAccessToken()

    override suspend fun publish(sleepNightPair: Pair<SleepNight, GoogleAccessToken>): GoogleFitSleepNightPublished {
        val sleepNight = sleepNightPair.first
        val googleAccessToken = sleepNightPair.second
        val identifier = idProvider identifier sleepNight
        client.put<Unit>("https://www.googleapis.com/fitness/v1/users/me/sessions/$identifier") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${googleAccessToken.accessToken}")
            body = GoogleFitPutSleepSessionHttpRequest(
                id = identifier,
                name = identifier,
                lastModifiedToken = identifier,
                description = "Polar Sleep Night",
                startTimeMillis = sleepNight.startTime.toEpochMilli(),
                endTimeMillis = sleepNight.endTime.toEpochMilli()
            )
        }
        return GoogleFitSleepNightPublished
    }
}

private data class GoogleAccessTokenHttpResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresInSeconds: Long,
    @SerializedName("refresh_token")
    val refreshToken: String,
) {
    fun toGoogleAccessToken(): GoogleAccessToken = GoogleAccessToken(
        accessToken = accessToken,
        expiresInSeconds = expiresInSeconds,
        refreshToken = refreshToken
    )
}


private data class GoogleFitPutSleepSessionHttpRequest(

    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("startTimeMillis") val startTimeMillis: Long,
    @SerializedName("endTimeMillis") val endTimeMillis: Long,
    @SerializedName("lastModifiedToken") val lastModifiedToken: String,
    @SerializedName("application") val application: Application = Application(),
    @SerializedName("version") val version: Int = 1,
    @SerializedName("activityType") val activityType: Int = 72,


    ) {
    private data class Application(

        @SerializedName("detailsUrl") val detailsUrl: String = "https://flow.polar.com",
        @SerializedName("name") val name: String = "Polar flow",
        @SerializedName("version") val version: String = "1.0",
    )
}


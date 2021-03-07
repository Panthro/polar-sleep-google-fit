package com.rafaelroman.infrastructure.clients

import com.google.gson.annotations.SerializedName
import com.rafaelroman.domain.googlefit.GoogleAccessToken
import com.rafaelroman.domain.googlefit.GoogleAccessTokenProvider
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.Parameters

class HttpGoogleAccessTokenProvider(
    private val client: HttpClient,
    private val clientSecret: String,
    private val clientId: String,
) : GoogleAccessTokenProvider {
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

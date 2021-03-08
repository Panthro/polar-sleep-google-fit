package com.rafaelroman.infrastructure.clients.google

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import com.rafaelroman.fixtures.buildGoogleAccessToken
import com.rafaelroman.fixtures.mockGoogleAccessTokenRequest
import com.rafaelroman.fixtures.mockHttpClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GoogleHttpClientTest {

    @Test
    fun `should retrieve access token`() = runBlocking {
        // Arrange
        val googleRequestCode = GoogleAuthorizationRequestCode(randomUUID().toString())
        val secret = randomUUID().toString()
        val clientId = randomUUID().toString()
        val googleAccessToken = buildGoogleAccessToken()
        val client = mockHttpClient(
            mockGoogleAccessTokenRequest(
                clientSecret = secret,
                clientId = clientId,
                code = googleRequestCode.code,
                accessToken = googleAccessToken.accessToken,
                expiresIn = googleAccessToken.expiresInSeconds,
                refreshToken = googleAccessToken.refreshToken,
            ),
        )
        val provider = GoogleHttpClient(
            client,
            clientId = clientId,
            clientSecret = secret
        )
        // Act
        val result = provider withCode googleRequestCode
        // Assert
        assertThat(result).isEqualTo(googleAccessToken)
    }
}

package com.rafaelroman.infrastructure.clients

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import com.rafaelroman.fixtures.buildPolarAccessToken
import com.rafaelroman.fixtures.mockHttpClient
import com.rafaelroman.fixtures.mockPolarAccessTokenRequest
import com.rafaelroman.fixtures.mockPolarNightsRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.security.SecureRandom
import java.util.UUID

internal class HttpPolarClientTest {
    @Test
    fun `should retrieve access token`() = runBlocking {
        // Arrange
        val polarAuthorizationRequestCode = PolarAuthorizationRequestCode(UUID.randomUUID().toString())
        val secret = UUID.randomUUID().toString()
        val clientId = UUID.randomUUID().toString()
        val accessToken = UUID.randomUUID().toString()
        val userId = SecureRandom().nextLong()
        val expiresIn = SecureRandom().nextLong()
        val provider = HttpPolarClient(
            client = mockHttpClient(
                mockPolarAccessTokenRequest(
                    code = polarAuthorizationRequestCode.value,
                    expiresIn = expiresIn,
                    userId = userId,
                    accessToken = accessToken,
                    clientId = clientId,
                    clientSecret = secret,
                ),
            ),
            clientId = clientId,
            clientSecret = secret
        )
        // Act

        val result = provider withCode polarAuthorizationRequestCode

        // Assert

        assertThat(result).isEqualTo(PolarAccessToken(accessToken, expiresIn, userId))
    }

    @Test
    fun `should retrieve latest sleep data`() = runBlocking {
        // Arrange
        val secret = UUID.randomUUID().toString()
        val clientId = UUID.randomUUID().toString()
        val accessToken = UUID.randomUUID().toString()
        val provider = HttpPolarClient(
            client = mockHttpClient(
                mockPolarNightsRequest(accessToken),
            ),
            clientId = clientId,
            clientSecret = secret
        )
        // Act
        val result = provider latest buildPolarAccessToken(accessToken = accessToken)

        // Assert
        assertThat(result).isNotEmpty()
    }
}

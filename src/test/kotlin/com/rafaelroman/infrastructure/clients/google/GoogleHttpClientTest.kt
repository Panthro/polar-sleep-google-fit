package com.rafaelroman.infrastructure.clients.google

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublished
import com.rafaelroman.fixtures.buildGoogleAccessToken
import com.rafaelroman.fixtures.buildSleepNight
import com.rafaelroman.fixtures.mockGoogleAccessTokenRequest
import com.rafaelroman.fixtures.mockGooglePutSleepNight
import com.rafaelroman.fixtures.mockHttpClient
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GoogleHttpClientTest {

    @Test
    fun `should retrieve access token`() = runBlocking {
        // Arrange
        val googleRequestCode = GoogleAuthorizationRequestCode(randomUUID().toString(), randomUUID().toString())
        val secret = randomUUID().toString()
        val clientId = randomUUID().toString()
        val googleAccessToken = buildGoogleAccessToken(polarUserId = googleRequestCode.polarUserId)
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

    @Test
    fun `should publish sleep nights to google fit`() = runBlocking {
        // Arrange
        val sleepNight = buildSleepNight()
        val secret = randomUUID().toString()
        val clientId = randomUUID().toString()
        val googleAccessToken = buildGoogleAccessToken()
        val sleepNightIdentifierProvider = mockk<SleepNightIdentifierProvider>()
        val identifier = randomUUID().toString()
        every {
            sleepNightIdentifierProvider identifier sleepNight
        } returns identifier
        val client = mockHttpClient(
            mockGooglePutSleepNight(
                accessToken = googleAccessToken.accessToken,
                identifier = identifier,
                sessionName = identifier,
                lastModifiedToken = identifier,
                description = "Polar Sleep Night",
                startTimeMillis = sleepNight.startTime.toEpochMilli(),
                endTimeMillis = sleepNight.endTime.toEpochMilli()
            )
        )
        val publisher = GoogleHttpClient(
            client,
            clientId = clientId,
            clientSecret = secret,
            idProvider = sleepNightIdentifierProvider
        )
        // Act
        val result = publisher publish (sleepNight to googleAccessToken)
        // Assert
        assertThat(result).isEqualTo(GoogleFitSleepNightPublished)
    }
}

package com.rafaelroman.application.syncpolarsleepdata

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublished
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublisher
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import com.rafaelroman.fixtures.buildGoogleAccessToken
import com.rafaelroman.fixtures.buildPolarAccessToken
import com.rafaelroman.fixtures.buildSleepNight
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class SyncPolarSleepDataUseCaseTest {

    @Test
    fun `should download data from polar and sync to google fit`() = runBlocking {
        // Arrange
        val polarAccessTokenRepository = mockk<PolarAccessTokenRepository>()
        val googleAccessTokenRepository = mockk<GoogleAccessTokenRepository>()
        val polarSleepDataProvider = mockk<PolarSleepDataProvider>()
        val googleFitSleepPublisher = mockk<GoogleFitSleepNightPublisher>()
        val usecase = SyncPolarSleepDataUseCase(
            polarAccessTokenRepository,
            polarSleepDataProvider,
            googleFitSleepPublisher,
            googleAccessTokenRepository
        )
        val polarAccessToken = buildPolarAccessToken()
        val googleAccessToken = buildGoogleAccessToken()
        val sleepNights = listOf(buildSleepNight() to googleAccessToken, buildSleepNight() to googleAccessToken)
        coEvery {
            polarAccessTokenRepository.current()
        } returns polarAccessToken
        coEvery {
            googleAccessTokenRepository.current()
        } returns googleAccessToken

        coEvery {
            polarSleepDataProvider latest polarAccessToken
        } returns sleepNights.map { it.first }

        coEvery {
            googleFitSleepPublisher publish any()
        } returns GoogleFitSleepNightPublished
        // Act
        val result = usecase.sync()
        // Assert
        assertThat(result).isEqualTo(SyncPolarSleepDataSuccessfully)

        sleepNights.forEach {
            coVerify {
                googleFitSleepPublisher publish it
            }
        }
    }
}

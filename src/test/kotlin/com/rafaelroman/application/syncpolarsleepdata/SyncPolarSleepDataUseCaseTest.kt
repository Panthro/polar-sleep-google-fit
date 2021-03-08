package com.rafaelroman.application.syncpolarsleepdata

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublisher
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import com.rafaelroman.fixtures.buildPolarAccessToken
import com.rafaelroman.fixtures.buildSleepNight
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class SyncPolarSleepDataUseCaseTest {

    @Test
    fun `should download data from polar and sync to google fit`() = runBlocking {
        // Arrange
        val polarAccessTokenRepository = mockk<PolarAccessTokenRepository>()
        val polarSleepDataProvider = mockk<PolarSleepDataProvider>()
        val googleFitSleepPublisher = mockk<GoogleFitSleepNightPublisher>()
        val usecase = SyncPolarSleepDataUseCase(polarAccessTokenRepository, polarSleepDataProvider, googleFitSleepPublisher)
        val polarAccessToken = buildPolarAccessToken()
        val sleepNights = listOf(buildSleepNight(), buildSleepNight())
        coEvery {
            polarAccessTokenRepository.current()
        } returns polarAccessToken

        coEvery {
            polarSleepDataProvider latest polarAccessToken
        } returns sleepNights

        coEvery {
            googleFitSleepPublisher publish any()
        } just Runs
        // Act
        val result = usecase.sync()
        // Assert
        assertThat(result).isEqualTo(SyncPolarSleepDataSuccessfully)

        sleepNights.forEach {
            verify {
                googleFitSleepPublisher publish it
            }
        }
    }
}

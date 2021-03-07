package com.rafaelroman.application.syncpolarsleepdata

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import com.rafaelroman.fixtures.buildPolarAccessToken
import com.rafaelroman.fixtures.buildPolarNight
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test


internal class SyncPolarSleepDataUseCaseTest {


    @Test
    fun `should download data from polar and sync to google fit`() = runBlocking {
        // Arrange
        val polarAccessTokenRepository = mockk<PolarAccessTokenRepository>()
        val polarSleepDataProvider = mockk<PolarSleepDataProvider>()
        val usecase = SyncPolarSleepDataUseCase(polarAccessTokenRepository, polarSleepDataProvider)
        val polarAccessToken = buildPolarAccessToken()
        val polarNights = listOf(buildPolarNight(), buildPolarNight())
        coEvery {
            polarAccessTokenRepository.current()
        } returns polarAccessToken

        coEvery {
            polarSleepDataProvider latest polarAccessToken
        } returns polarNights
        // Act
        val result = usecase.sync()
        // Assert
        assertThat(result).isEqualTo(SyncPolarSleepDataSuccessfully)
    }
}



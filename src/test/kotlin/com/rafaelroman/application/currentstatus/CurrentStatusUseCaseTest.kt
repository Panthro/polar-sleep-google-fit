package com.rafaelroman.application.currentstatus

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.fixtures.buildGoogleAccessToken
import com.rafaelroman.fixtures.buildPolarAccessToken
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class CurrentStatusUseCaseTest {


    @Test
    fun `should return google authenticated and polar authenticated`() = runBlocking {
        // Arrange
        val googleAccessTokenRepository: GoogleAccessTokenRepository = mockk()
        val polarAccessTokenRepository: PolarAccessTokenRepository = mockk()
        val useCase = CurrentStatusUseCase(googleAccessTokenRepository, polarAccessTokenRepository)

        coEvery {
            googleAccessTokenRepository.current()
        } returns buildGoogleAccessToken()

        coEvery {
            polarAccessTokenRepository.current()
        } returns buildPolarAccessToken()

        // Act
        val result = useCase.status()
        // Assert
        assertThat(result).isEqualTo(
            CurrentStatus(
                googleAuthStatus = GoogleAuthStatus.Authenticated,
                polarAuthStatus = PolarAuthStatus.Authenticated
            )
        )
    }

    @Test
    fun `should return google unauthenticated and polar unauthenticated`() = runBlocking {
        // Arrange
        val googleAccessTokenRepository: GoogleAccessTokenRepository = mockk()
        val polarAccessTokenRepository: PolarAccessTokenRepository = mockk()
        val useCase = CurrentStatusUseCase(googleAccessTokenRepository, polarAccessTokenRepository)

        coEvery {
            googleAccessTokenRepository.current()
        } returns null

        coEvery {
            polarAccessTokenRepository.current()
        } returns null

        // Act
        val result = useCase.status()
        // Assert
        assertThat(result).isEqualTo(
            CurrentStatus(
                googleAuthStatus = GoogleAuthStatus.Unauthenticated,
                polarAuthStatus = PolarAuthStatus.Unauthenticated
            )
        )
    }
}

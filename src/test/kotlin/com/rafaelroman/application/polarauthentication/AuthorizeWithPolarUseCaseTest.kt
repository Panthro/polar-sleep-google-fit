package com.rafaelroman.application.polarauthentication

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import com.rafaelroman.domain.polar.PolarAccessCodeProvider
import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.fixtures.buildPolarAccessToken
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID

internal class AuthorizeWithPolarUseCaseTest {


    @Test
    fun `should authorise with polar`() = runBlocking {
        // Arrange
        val authorizationRequestCode = PolarAuthorizationRequestCode(UUID.randomUUID().toString())
        val polarAccessToken = buildPolarAccessToken()

        val polarAccessCodeProvider = mockk<PolarAccessCodeProvider>()
        val polarAccessTokenRepository = mockk<PolarAccessTokenRepository>()
        val useCase = AuthorizeWithPolarUseCase(polarAccessCodeProvider, polarAccessTokenRepository)

        coEvery {
            polarAccessCodeProvider withCode authorizationRequestCode
        } returns polarAccessToken

        coEvery {
            polarAccessTokenRepository save polarAccessToken
        } just Runs

        // Act
        val result = useCase authorize authorizationRequestCode

        // Assert
        assertThat(result).isEqualTo(AuthorizeWithPolarSuccessfully)
        verify {
            polarAccessTokenRepository.save(polarAccessToken)
        }

    }
}

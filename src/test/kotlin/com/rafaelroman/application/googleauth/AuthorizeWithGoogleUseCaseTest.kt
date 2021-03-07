package com.rafaelroman.application.googleauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.domain.googlefit.GoogleAccessTokenProvider
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import com.rafaelroman.fixtures.buildGoogleAccessToken
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class AuthorizeWithGoogleUseCaseTest {


    @Test
    fun `should authorize with google`() = runBlocking{
        // Arrange
        val authRequestCode = GoogleAuthorizationRequestCode(randomUUID().toString())
        val googleAccessToken = buildGoogleAccessToken()
        val googleAccessCodeProvider = mockk<GoogleAccessTokenProvider>()
        val googleAccessTokenRepository = mockk<GoogleAccessTokenRepository>()
        val useCase = AuthorizeWithGoogleUseCase(googleAccessCodeProvider, googleAccessTokenRepository)

        coEvery {
            googleAccessCodeProvider withCode authRequestCode
        } returns googleAccessToken

        coEvery {
            googleAccessTokenRepository save googleAccessToken
        } just Runs

        // Act
        val result = useCase authorize authRequestCode
        // Assert
        assertThat(result).isEqualTo(AuthorizeWithGoogleSuccessfully)
        coVerify {
            googleAccessTokenRepository save googleAccessToken
        }
    }


}

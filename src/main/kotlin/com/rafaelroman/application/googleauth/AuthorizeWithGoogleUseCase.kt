package com.rafaelroman.application.googleauth

import com.rafaelroman.domain.googlefit.GoogleAccessTokenProvider
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleAuthorizationRequestCode
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(AuthorizeWithGoogleUseCase::class.java)

class AuthorizeWithGoogleUseCase(
    private val googleAccessCodeProvider: GoogleAccessTokenProvider,
    private val googleAccessTokenRepository: GoogleAccessTokenRepository,
) {
    suspend infix fun authorize(authRequestCode: GoogleAuthorizationRequestCode): AuthorizeWithGoogleSuccessfully =
        (googleAccessCodeProvider withCode authRequestCode)
            .apply {
                logger.info("process=AuthorizeWithGoogleUseCase status=success accessToken=$this")
            }.also { googleAccessTokenRepository save it }
            .let { AuthorizeWithGoogleSuccessfully }
}

object AuthorizeWithGoogleSuccessfully

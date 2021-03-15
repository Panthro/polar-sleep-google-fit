package com.rafaelroman.application.polarauthentication

import com.rafaelroman.domain.polar.PolarAccessCodeProvider
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(AuthorizeWithPolarUseCase::class.java)

class AuthorizeWithPolarUseCase(
    private val polarAccessCodeProvider: PolarAccessCodeProvider,
    private val polarAccessTokenRepository: PolarAccessTokenRepository,
) {

    suspend infix fun authorize(polarCode: PolarAuthorizationRequestCode) =
        (polarAccessCodeProvider withCode polarCode)
            .apply {
                logger.info("process=AuthorizeWithPolarUseCase status=success accessToken=$this")
            }
            .apply { polarAccessTokenRepository save this }
}

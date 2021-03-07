package com.rafaelroman.application.currentstatus

import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CurrentStatusUseCase::class.java)

class CurrentStatusUseCase(
    private val googleAccessTokenRepository: GoogleAccessTokenRepository,
    private val polarAccessTokenRepository: PolarAccessTokenRepository,
) {
    suspend fun status(): CurrentStatus = coroutineScope {
        val google = async { googleAccessTokenRepository.current() }
        val polar = async { polarAccessTokenRepository.current() }
        CurrentStatus(
            googleAuthStatus = google.await()?.let { GoogleAuthStatus.Authenticated } ?: GoogleAuthStatus.Unauthenticated,
            polarAuthStatus = polar.await()?.let { PolarAuthStatus.Authenticated } ?: PolarAuthStatus.Unauthenticated
        ).also {
            logger.info("process=current-status status=$it")
        }

    }
}

data class CurrentStatus(
    val googleAuthStatus: GoogleAuthStatus,
    val polarAuthStatus: PolarAuthStatus,
)

sealed class GoogleAuthStatus {
    object Authenticated : GoogleAuthStatus()
    object Unauthenticated : GoogleAuthStatus()
}

sealed class PolarAuthStatus {
    object Authenticated : PolarAuthStatus()
    object Unauthenticated : PolarAuthStatus()
}


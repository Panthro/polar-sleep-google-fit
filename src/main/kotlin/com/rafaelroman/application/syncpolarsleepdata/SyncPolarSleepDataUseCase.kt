package com.rafaelroman.application.syncpolarsleepdata

import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublisher
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(SyncPolarSleepDataUseCase::class.java)

class SyncPolarSleepDataUseCase(
    private val polarAccessTokenRepository: PolarAccessTokenRepository,
    private val polarSleepDataProvider: PolarSleepDataProvider,
    private val googleFitSleepPublisher: GoogleFitSleepNightPublisher,
    private val googleAccessTokenRepository: GoogleAccessTokenRepository
) {

    suspend fun sync(): SyncPolarSleepDataSuccessfully {
        polarAccessTokenRepository.current().let { accessToken ->
            polarSleepDataProvider latest accessToken!!
        }.apply {
            logger.info("process=sync-polar-nights status=nights-returned nights=${this.size}")
        }.forEach {
            googleFitSleepPublisher publish (it to googleAccessTokenRepository.current()!!)
            logger.info("process=sync-polar-nights status=sync-night night=$it")
        }.also {
            logger.info("process=sync-polar-nights status=finished")
        }
        return SyncPolarSleepDataSuccessfully
    }
}

object SyncPolarSleepDataSuccessfully

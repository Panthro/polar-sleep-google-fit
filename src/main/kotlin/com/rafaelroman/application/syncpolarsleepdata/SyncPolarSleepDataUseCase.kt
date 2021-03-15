package com.rafaelroman.application.syncpolarsleepdata

import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import com.rafaelroman.domain.googlefit.GoogleFitSleepNightPublisher
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(SyncPolarSleepDataUseCase::class.java)

class SyncPolarSleepDataUseCase(
    private val polarAccessTokenRepository: PolarAccessTokenRepository,
    private val polarSleepDataProvider: PolarSleepDataProvider,
    private val googleFitSleepPublisher: GoogleFitSleepNightPublisher,
    private val googleAccessTokenRepository: GoogleAccessTokenRepository,
) {

    suspend fun sync(userId: String): SyncPolarSleepDataSuccessfully {
        coroutineScope {
            val polarAccessToken = polarAccessTokenRepository.find(userId)!!
            val googleAccessToken = googleAccessTokenRepository.find(userId)!!

            val nights = polarSleepDataProvider latest polarAccessToken
            logger.info("process=sync-polar-nights status=nights-returned nights=${nights.size}")
            nights.map { night ->
                async {
                    googleFitSleepPublisher publish (night to googleAccessToken)
                    logger.info("process=sync-polar-nights status=sync-night night=$night")
                }
            }.forEach { it.await() }
            logger.info("process=sync-polar-nights status=finished")
        }
        return SyncPolarSleepDataSuccessfully
    }
}

object SyncPolarSleepDataSuccessfully

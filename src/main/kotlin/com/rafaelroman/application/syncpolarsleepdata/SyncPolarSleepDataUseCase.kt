package com.rafaelroman.application.syncpolarsleepdata

import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(SyncPolarSleepDataUseCase::class.java)

class SyncPolarSleepDataUseCase(
    private val polarAccessTokenRepository: PolarAccessTokenRepository,
    private val polarSleepDataProvider: PolarSleepDataProvider,
) {

    suspend fun sync(): SyncPolarSleepDataSuccessfully {
        polarAccessTokenRepository.current().let { accessToken ->
            polarSleepDataProvider latest accessToken!!
        }.apply {
            logger.info("process=sync-polar-nights status=nights-returned nights=${this.size}")
        }
        //TODO sync with google
        return SyncPolarSleepDataSuccessfully
    }
}

object SyncPolarSleepDataSuccessfully

package com.rafaelroman.infrastructure.persistence

import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAccessTokenRepository

class InMemoryPolarAccessTokenRepository : PolarAccessTokenRepository {

    private var current: PolarAccessToken? = null


    override suspend fun save(polarAccessToken: PolarAccessToken) {
        current = polarAccessToken
    }

    override suspend fun current(): PolarAccessToken? = current

}

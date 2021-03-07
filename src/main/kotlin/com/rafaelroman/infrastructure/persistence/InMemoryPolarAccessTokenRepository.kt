package com.rafaelroman.infrastructure.persistence

import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAccessTokenRepository

class InMemoryPolarAccessTokenRepository : PolarAccessTokenRepository {

    private var current: PolarAccessToken? = null


    override fun save(polarAccessToken: PolarAccessToken) {
        current = polarAccessToken
    }

    override fun current(): PolarAccessToken? = current

}

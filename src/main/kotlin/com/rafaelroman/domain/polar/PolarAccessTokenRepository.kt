package com.rafaelroman.domain.polar

interface PolarAccessTokenRepository {
    suspend infix fun save(polarAccessToken: PolarAccessToken)
    suspend fun current(): PolarAccessToken?
}

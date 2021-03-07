package com.rafaelroman.domain.polar

interface PolarAccessTokenRepository {
    infix fun save(polarAccessToken: PolarAccessToken)
    fun current(): PolarAccessToken?

}

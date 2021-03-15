package com.rafaelroman.domain.googlefit

interface GoogleAccessTokenRepository {
    suspend infix fun save(googleAccessToken: GoogleAccessToken)
    suspend fun find(polarUserId: String): GoogleAccessToken?
}

package com.rafaelroman.domain.googlefit

data class GoogleAccessToken(
    val accessToken: String,
    val expiresInSeconds: Long,
    val refreshToken: String,
)

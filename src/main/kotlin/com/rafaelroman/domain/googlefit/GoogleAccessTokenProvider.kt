package com.rafaelroman.domain.googlefit

interface GoogleAccessTokenProvider {
    suspend infix fun withCode(googleAuthorizationRequestCode: GoogleAuthorizationRequestCode): GoogleAccessToken

}

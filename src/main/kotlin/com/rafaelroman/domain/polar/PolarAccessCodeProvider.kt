package com.rafaelroman.domain.polar

interface PolarAccessCodeProvider {
    suspend infix fun withCode(polarAuthorizationRequestCode: PolarAuthorizationRequestCode) : PolarAccessToken

}

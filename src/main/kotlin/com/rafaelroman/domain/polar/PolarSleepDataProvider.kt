package com.rafaelroman.domain.polar

interface PolarSleepDataProvider {
    suspend infix fun latest(polarAccessToken: PolarAccessToken): List<PolarSleepNight>
}




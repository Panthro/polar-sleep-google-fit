package com.rafaelroman.domain.polar

import com.rafaelroman.domain.sleep.SleepNight

interface PolarSleepDataProvider {
    suspend infix fun latest(polarAccessToken: PolarAccessToken): List<SleepNight>
}

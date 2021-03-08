package com.rafaelroman.domain.googlefit

import com.rafaelroman.domain.sleep.SleepNight

interface GoogleFitSleepNightPublisher {
    suspend infix fun publish(sleepNightPair: Pair<SleepNight, GoogleAccessToken>): GoogleFitSleepNightPublished
}

object GoogleFitSleepNightPublished

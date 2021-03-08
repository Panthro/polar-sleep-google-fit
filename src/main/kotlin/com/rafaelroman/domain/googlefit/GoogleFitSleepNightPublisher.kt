package com.rafaelroman.domain.googlefit

import com.rafaelroman.domain.sleep.SleepNight

interface GoogleFitSleepNightPublisher {
    infix fun publish(sleepNight: SleepNight)
}

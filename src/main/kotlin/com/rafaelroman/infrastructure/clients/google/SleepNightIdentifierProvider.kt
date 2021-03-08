package com.rafaelroman.infrastructure.clients.google

import com.rafaelroman.domain.sleep.SleepNight
import java.util.UUID

object SleepNightIdentifierProvider {
    infix fun identifier(sleepNight: SleepNight): String = UUID.nameUUIDFromBytes(
        (sleepNight.startTime.toString().toByteArray() + sleepNight.endTime.toString().toByteArray())
    ).toString()
}

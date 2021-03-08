package com.rafaelroman.fixtures

import com.rafaelroman.domain.googlefit.GoogleAccessToken
import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.sleep.SleepNight
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

fun buildPolarAccessToken(accessToken: String = UUID.randomUUID().toString()) = PolarAccessToken(
    accessToken = accessToken,
    expiresIn = TimeUnit.HOURS.toMillis(1),
    userId = SecureRandom().nextLong()
)

fun buildGoogleAccessToken() = GoogleAccessToken(
    accessToken = UUID.randomUUID().toString(),
    expiresInSeconds = SecureRandom().nextLong(),
    refreshToken = UUID.randomUUID().toString(),
)

fun buildSleepNight(
    startTime: Instant = Instant.now(),
    endTime: Instant = Instant.now(),
) = SleepNight(
    startTime = startTime,
    endTime = endTime
)

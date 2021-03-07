package com.rafaelroman.fixtures

import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarSleepNight
import io.ktor.http.Url
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.TimeUnit

private val random = SecureRandom()

fun buildPolarAccessToken(accessToken: String = UUID.randomUUID().toString()) = PolarAccessToken(
    accessToken = accessToken,
    expiresIn = TimeUnit.HOURS.toMillis(1),
    userId = SecureRandom().nextLong()
)

fun buildPolarNight() = PolarSleepNight(
    polarUser = Url("https://www.polaraccesslink/v3/users/1"),
    deviceID = "1111AAAA",
    date = "2020-01-01",
    sleepStartTime = "2020-01-01T00:39:07+03:00",
    sleepEndTime = "2020-01-01T09:19:37+03:00",
    continuity = random.nextDouble(),
    continuityClass = random.nextLong(),
    lightSleep = random.nextLong(),
    deepSleep = random.nextLong(),
    remSleep = random.nextLong(),
    unrecognizedSleepStage = random.nextLong(),
    sleepScore = random.nextLong(),
    totalInterruptionDuration = random.nextLong(),
    sleepCharge = random.nextLong(),
    sleepGoal = random.nextLong(),
    sleepRating = random.nextLong(),
    shortInterruptionDuration = random.nextLong(),
    longInterruptionDuration = random.nextLong(),
    sleepCycles = random.nextLong(),
    groupDurationScore = random.nextLong(),
    groupSolidityScore = random.nextLong(),
    groupRegenerationScore = random.nextDouble(),
    hypnogram = mapOf("00:39" to 2,
        "00:50" to 3,
        "01:23" to 6),
    heartRateSamples = mapOf("00:41" to 76,
        "00:46" to 77,
        "00:51" to 76),
)

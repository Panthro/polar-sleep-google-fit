package com.rafaelroman.domain.polar

import io.ktor.http.Url

data class PolarSleepNight(
    val polarUser: Url,
    val date: String,
    val sleepStartTime: String,
    val sleepEndTime: String,
    val deviceID: String,
    val continuity: Double,
    val continuityClass: Long,
    val lightSleep: Long,
    val deepSleep: Long,
    val remSleep: Long,
    val unrecognizedSleepStage: Long,
    val sleepScore: Long,
    val totalInterruptionDuration: Long,
    val sleepCharge: Long,
    val sleepGoal: Long,
    val sleepRating: Long,
    val shortInterruptionDuration: Long,
    val longInterruptionDuration: Long,
    val sleepCycles: Long,
    val groupDurationScore: Long,
    val groupSolidityScore: Long,
    val groupRegenerationScore: Double,
    val hypnogram: Map<String, Long>?,
    val heartRateSamples: Map<String, Long>?,
)

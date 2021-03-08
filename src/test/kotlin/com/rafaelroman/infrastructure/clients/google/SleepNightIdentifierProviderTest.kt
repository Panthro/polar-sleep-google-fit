package com.rafaelroman.infrastructure.clients.google

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.rafaelroman.fixtures.buildSleepNight
import org.junit.jupiter.api.Test

internal class SleepNightIdentifierProviderTest{


    @Test
    fun `should provide the same id given the same sleep night`() {
        // Arrange
        val sleepNight = buildSleepNight()
        // Act
        val firstId = SleepNightIdentifierProvider identifier sleepNight
        val secondId = SleepNightIdentifierProvider identifier sleepNight
        // Assert
        assertThat(firstId).isEqualTo(secondId)
    }

    @Test
    fun `should provide different ids`() {
        // Arrange
        val firstSleepNight = buildSleepNight()
        val secondSleepNight = buildSleepNight()
        // Act
        val firstId = SleepNightIdentifierProvider identifier firstSleepNight
        val secondId = SleepNightIdentifierProvider identifier secondSleepNight
        // Assert
        assertThat(firstId).isNotEqualTo(secondId)
    }
}

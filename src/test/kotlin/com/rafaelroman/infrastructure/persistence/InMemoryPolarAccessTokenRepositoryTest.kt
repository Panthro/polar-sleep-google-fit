package com.rafaelroman.infrastructure.persistence

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.rafaelroman.fixtures.buildPolarAccessToken
import org.junit.jupiter.api.Test

internal class InMemoryPolarAccessTokenRepositoryTest{


    @Test
    fun `should save polar access token`() {
        // Arrange
        val polarAccessToken = buildPolarAccessToken()
        val repository = InMemoryPolarAccessTokenRepository()
        // Act
        repository save polarAccessToken
        // Assert
        assertThat(repository.current()).isEqualTo(polarAccessToken)
    }
}

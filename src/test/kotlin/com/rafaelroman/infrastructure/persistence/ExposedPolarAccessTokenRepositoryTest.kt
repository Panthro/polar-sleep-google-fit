package com.rafaelroman.infrastructure.persistence

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.fixtures.buildPolarAccessToken
import org.junit.jupiter.api.Test

internal class ExposedPolarAccessTokenRepositoryTest{
    @Test
    fun `should save polar access token`() {
        // Arrange
        val polarAccessToken = buildPolarAccessToken()
        val repository = ExposedPolarAccessTokenRepository()
        // Act
        repository save polarAccessToken
        // Assert
        assertThat(repository.current()).isEqualTo(polarAccessToken)
    }
}

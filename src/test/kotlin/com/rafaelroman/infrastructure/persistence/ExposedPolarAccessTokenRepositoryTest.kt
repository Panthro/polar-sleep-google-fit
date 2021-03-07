package com.rafaelroman.infrastructure.persistence

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.fixtures.buildPolarAccessToken
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test

internal class ExposedPolarAccessTokenRepositoryTest {
    @Test
    fun `should save polar access token`() = runBlocking {
        // Arrange
        val db = Database.connect("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        val polarAccessToken = buildPolarAccessToken()
        val repository = ExposedPolarAccessTokenRepository(db)
        // Act
        repository save polarAccessToken
        // Assert
        assertThat(repository.current()).isEqualTo(polarAccessToken)
    }
}

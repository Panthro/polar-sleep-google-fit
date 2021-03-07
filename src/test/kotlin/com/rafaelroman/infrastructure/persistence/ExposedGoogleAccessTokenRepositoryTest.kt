package com.rafaelroman.infrastructure.persistence

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.rafaelroman.fixtures.buildGoogleAccessToken
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test

internal class ExposedGoogleAccessTokenRepositoryTest {
    @Test
    fun `should save google access token`(): Unit = runBlocking {
        // Arrange
        val db = Database.connect("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        val googleAccessToken = buildGoogleAccessToken()
        val repository = ExposedGoogleAccessTokenRepository(db)


        // Act
        repository save googleAccessToken
        // Assert
        assertThat(repository.current()).isEqualTo(googleAccessToken)
    }
}

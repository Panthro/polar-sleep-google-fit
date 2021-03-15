package com.rafaelroman

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matchesPredicate
import com.rafaelroman.infrastructure.persistence.GoogleAccessTokenTable
import com.rafaelroman.infrastructure.persistence.PolarAccessTokenTable
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.UUID

class ApplicationTest {
    @Test
    fun `should load buttons when google and polar are unauthenticated`() {
        withTestApplication({
            testEnvironment()
        }) {
            transaction {
                GoogleAccessTokenTable.deleteAll()
                PolarAccessTokenTable.deleteAll()
            }
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).matchesPredicate {
                    it!!.contains("Connect polar")
                }
            }
        }
    }
}

private fun Application.testEnvironment() {
    (environment.config as MapApplicationConfig).apply {
        put("db.url", "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1")
        put("polar.oauth2.clientId", UUID.randomUUID().toString())
        put("polar.oauth2.clientSecret", UUID.randomUUID().toString())
        put("google.oauth2.clientId", UUID.randomUUID().toString())
        put("google.oauth2.clientSecret", UUID.randomUUID().toString())
    }
    module(testing = true)
}

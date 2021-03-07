package com.rafaelroman

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matchesPredicate
import io.ktor.application.*
import io.ktor.config.MapApplicationConfig
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.slf4j.event.*
import java.util.UUID

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("polar.oauth2.clientId", UUID.randomUUID().toString())
                put("polar.oauth2.clientSecret", UUID.randomUUID().toString())
                put("google.oauth2.clientId", UUID.randomUUID().toString())
                put("google.oauth2.clientSecret", UUID.randomUUID().toString())
            }
            module(testing = true)
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).matchesPredicate {
                    it!!.contains("Connect polar")
                    it.contains("Connect Google")
                }
            }
        }
    }
}

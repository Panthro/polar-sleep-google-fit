package com.rafaelroman.infrastructure.persistence

import com.rafaelroman.domain.googlefit.GoogleAccessToken
import com.rafaelroman.domain.googlefit.GoogleAccessTokenRepository
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedGoogleAccessTokenRepository(private val db: Database) : GoogleAccessTokenRepository {
    init {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(GoogleAccessTokenTable)
        }
    }

    override suspend infix fun save(googleAccessToken: GoogleAccessToken) {
        transaction(db) {
            GoogleAccessTokenTable.deleteWhere {
                GoogleAccessTokenTable.polarUserId eq googleAccessToken.polarUserId
            }
            GoogleAccessTokenDao.new {
                accessToken = googleAccessToken.accessToken
                expiresIn = googleAccessToken.expiresInSeconds
                refreshToken = googleAccessToken.refreshToken
                polarUserId = googleAccessToken.polarUserId
            }
        }
    }

    override suspend fun find(polarUserId: String): GoogleAccessToken? = transaction(db) {
        GoogleAccessTokenDao.find {
            GoogleAccessTokenTable.polarUserId eq polarUserId
        }.limit(1).firstOrNull()?.let {
            GoogleAccessToken(
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                expiresInSeconds = it.expiresIn,
                polarUserId = it.polarUserId
            )
        }
    }
}

object GoogleAccessTokenTable : LongIdTable() {
    val accessToken = varchar("accessToken", 256)
    val expiresIn = long("expiresIn")
    val refreshToken = varchar("refreshToken", 256)
    val polarUserId = varchar("polarUserId", 256)
}

class GoogleAccessTokenDao(userId: EntityID<Long>) : LongEntity(userId) {
    companion object : LongEntityClass<GoogleAccessTokenDao>(GoogleAccessTokenTable)

    var accessToken by GoogleAccessTokenTable.accessToken
    var expiresIn by GoogleAccessTokenTable.expiresIn
    var refreshToken by GoogleAccessTokenTable.refreshToken
    var polarUserId by GoogleAccessTokenTable.polarUserId
}

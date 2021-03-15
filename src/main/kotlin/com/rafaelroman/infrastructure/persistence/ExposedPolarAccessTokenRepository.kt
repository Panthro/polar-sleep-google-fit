package com.rafaelroman.infrastructure.persistence

import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
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

class ExposedPolarAccessTokenRepository(
    private val db: Database,
) : PolarAccessTokenRepository {

    init {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(PolarAccessTokenTable)
        }
    }

    override suspend fun save(polarAccessToken: PolarAccessToken) {
        transaction(db) {
            PolarAccessTokenTable.deleteWhere {
                PolarAccessTokenTable.polarUserId eq polarAccessToken.userId
            }
            PolarAccessTokenDao.new() {
                accessToken = polarAccessToken.accessToken
                expiresIn = polarAccessToken.expiresIn
                polarUserId = polarAccessToken.userId
            }
        }
    }

    override suspend fun find(userId: String): PolarAccessToken? = transaction(db) {
        PolarAccessTokenDao.find {
            PolarAccessTokenTable.polarUserId eq userId
        }.limit(1).firstOrNull()?.let {
            PolarAccessToken(
                it.accessToken,
                it.expiresIn,
                it.polarUserId
            )
        }
    }
}

object PolarAccessTokenTable : LongIdTable() {
    val accessToken = varchar("accessToken", 256)
    val expiresIn = long("expiresIn")
    val polarUserId = varchar("polarUserId", 256).uniqueIndex()
}

class PolarAccessTokenDao(userId: EntityID<Long>) : LongEntity(userId) {
    companion object : LongEntityClass<PolarAccessTokenDao>(PolarAccessTokenTable)

    var accessToken by PolarAccessTokenTable.accessToken
    var expiresIn by PolarAccessTokenTable.expiresIn
    var polarUserId by PolarAccessTokenTable.polarUserId
}

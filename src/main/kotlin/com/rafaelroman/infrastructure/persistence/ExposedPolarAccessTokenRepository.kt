package com.rafaelroman.infrastructure.persistence

import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAccessTokenRepository
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
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
            addLogger(StdOutSqlLogger)
            PolarAccessTokenTable.deleteAll()
            PolarAccessTokenDao.new(polarAccessToken.userId) {
                accessToken = polarAccessToken.accessToken
                expiresIn = polarAccessToken.expiresIn
            }
        }
    }

    override suspend fun current(): PolarAccessToken? = transaction(db) {
        PolarAccessTokenDao.all().limit(1)
            .run {
                if (!empty()) {
                    first().let {
                        PolarAccessToken(
                            it.accessToken,
                            it.expiresIn,
                            it.polarUserId.value
                        )
                    }
                } else {
                    null
                }
            }
    }
}

object PolarAccessTokenTable : IdTable<Long>() {
    val accessToken = varchar("accessToken", 256)
    val expiresIn = long("expiresIn")
    val polarUserId = long("polarUserId").entityId()

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(polarUserId) }
    override val id: Column<EntityID<Long>>
        get() = polarUserId
}

class PolarAccessTokenDao(userId: EntityID<Long>) : LongEntity(userId) {
    companion object : LongEntityClass<PolarAccessTokenDao>(PolarAccessTokenTable)

    var accessToken by PolarAccessTokenTable.accessToken
    var expiresIn by PolarAccessTokenTable.expiresIn
    var polarUserId by PolarAccessTokenTable.polarUserId
}

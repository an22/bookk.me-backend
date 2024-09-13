package com.book.user.data.repository

import com.book.core.data.BaseDataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.book.user.data.map.toUser
import com.book.user.data.orm.UserColumn
import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.entity.User
import org.ktorm.database.Database
import org.ktorm.dsl.*

internal class UserAuthLocalDataSourceImpl(
    private val database: Database,
    private val cacheClient: CacheClient<String>
) : BaseDataSource(), UserLocalDataSource {

    override suspend fun insertNewUser(user: User) = execute {
        database.insertAndGenerateKey(UserColumn) {
            set(it.name, user.name)
            set(it.lastName, user.lastName)
            set(it.email, user.email)
            set(it.phone, user.phone)
            set(it.role, user.role.id)
        } as Long
    }

    override suspend fun getUserById(id: Long): User? = execute {
        val cached: User? = cacheClient.get("${USER_CACHE_KEY}$id")
        if (cached != null) return@execute cached
        database.from(UserColumn)
            .select()
            .where { UserColumn.id eq id }
            .map { it.toUser() }
            .firstOrNull()
            ?.also { cacheClient.set("${USER_CACHE_KEY}$id", it) }
    }

    override suspend fun getUserByPhoneOrEmail(phone: String, email: String): User? = execute {
        database.from(UserColumn)
            .select()
            .where { (UserColumn.phone eq phone) or (UserColumn.email eq email) }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun deleteUser(id: Long) {
        database.delete(UserColumn) {
            it.id eq id
        }
        cacheClient.delete("${USER_CACHE_KEY}$id")
    }

    companion object {
        const val USER_CACHE_KEY = "user:"
    }
}
package com.book.auth.data.repository

import com.book.auth.data.map.toDeviceAuthRecord
import com.book.auth.data.map.toDeviceInfo
import com.book.auth.data.map.toUserAuthRecord
import com.book.auth.data.orm.AuthDevice
import com.book.auth.data.orm.UserAuthInfo
import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.entity.DeviceAuthRecord
import com.book.auth.domain.api.entity.DeviceInfo
import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.entity.UserAuthRecord
import com.book.core.data.BaseDataSource
import com.book.core.data.cache.CacheClient
import org.ktorm.database.Database
import org.ktorm.dsl.*

internal class UserAuthLocalDataSourceImpl(
    private val database: Database,
    private val cacheClient: CacheClient<String>
) : BaseDataSource(), UserAuthLocalDataSource {
    override suspend fun saveUserRefreshToken(deviceId: Long, token: String) {
        execute {
            database.update(AuthDevice) {
                set(it.isSignedIn, true)
                set(it.refreshToken, token)
                where { it.id eq deviceId }
            }
        }
    }

    override suspend fun getDeviceAuthRecord(
        deviceName: String,
        login: String,
        passwordHash: String
    ): DeviceAuthRecord? = execute {
        database.from(AuthDevice)
            .innerJoin(UserAuthInfo, on = AuthDevice.userAuthId eq UserAuthInfo.id)
            .select()
            .where {
                (AuthDevice.deviceName eq deviceName) and
                        (UserAuthInfo.login eq login) and
                        (UserAuthInfo.passwordHash eq passwordHash)
            }
            .map(QueryRowSet::toDeviceAuthRecord)
            .firstOrNull()
    }

    override suspend fun getAuthRecordByUsername(login: String): UserAuthRecord? = execute {
        database.from(UserAuthInfo)
            .select()
            .where { (UserAuthInfo.login eq login) }
            .map(QueryRowSet::toUserAuthRecord)
            .firstOrNull()
    }

    override suspend fun createAuthRecord(
        userId: Long,
        passwordHash: String,
        totpSecret: String,
        info: SignUpInfo
    ) {
        execute {
            database.insertAndGenerateKey(UserAuthInfo) {
                set(it.userId, userId)
                set(it.login, info.login)
                set(it.passwordHash, passwordHash)
                set(it.totpSecret, totpSecret)
                set(it.role, info.role.id)
            }
        }
    }

    override suspend fun getDeviceAuthRecord(deviceId: Long, refreshToken: String): DeviceAuthRecord? = execute {
        database.from(AuthDevice)
            .innerJoin(UserAuthInfo, on = AuthDevice.userAuthId eq UserAuthInfo.id)
            .select()
            .where { (AuthDevice.id eq deviceId) and (AuthDevice.refreshToken eq refreshToken) }
            .map(QueryRowSet::toDeviceAuthRecord)
            .firstOrNull()
    }

    override suspend fun getDevices(authRecordId: Long): List<DeviceInfo> = execute {
        database.from(AuthDevice)
            .select()
            .where { AuthDevice.userAuthId eq authRecordId }
            .map(QueryRowSet::toDeviceInfo)
    }

    override suspend fun getDevice(authRecordId: Long, deviceName: String): DeviceInfo? = execute {
        database.from(AuthDevice)
            .select()
            .where { (AuthDevice.userAuthId eq authRecordId) and (AuthDevice.deviceName eq deviceName) }
            .map(QueryRowSet::toDeviceInfo)
            .firstOrNull()
    }

    override suspend fun deleteTokenInfoForDevice(deviceId: Long) {
        execute {
            database.update(AuthDevice) {
                set(it.refreshToken, "")
                set(it.isSignedIn, false)
                where { it.id eq deviceId }
            }
        }
    }

    override suspend fun createDevice(authRecordId: Long, deviceName: String): Long = execute {
        database.insertAndGenerateKey(AuthDevice) {
            set(it.userAuthId, authRecordId)
            set(it.deviceName, deviceName)
            set(it.refreshToken, "")
            set(it.isSignedIn, false)
        } as Long
    }

    override suspend fun deleteAccount(userId: Long) {
        execute {
            database.delete(UserAuthInfo) {
                it.userId eq userId
            }
        }
    }
}
package com.book.business.domain.api.operation

import com.book.business.domain.api.entity.UserBusinesses
import kotlin.uuid.Uuid

interface GetUserBusinesses {
    suspend operator fun invoke(userId: Uuid): Result<UserBusinesses>
}
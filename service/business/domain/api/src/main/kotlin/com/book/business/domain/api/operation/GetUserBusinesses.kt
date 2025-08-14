package com.book.business.domain.api.operation

import com.book.business.domain.api.entity.UserBusinesses

interface GetUserBusinesses {
    suspend operator fun invoke(userId: Long): Result<UserBusinesses>
}
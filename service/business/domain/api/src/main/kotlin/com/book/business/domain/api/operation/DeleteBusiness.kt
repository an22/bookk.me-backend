package com.book.business.domain.api.operation

interface DeleteBusiness {
    suspend operator fun invoke(userId: Long): Result<Unit>
}
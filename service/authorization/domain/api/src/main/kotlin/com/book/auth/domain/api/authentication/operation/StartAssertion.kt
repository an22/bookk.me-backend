package com.book.auth.domain.api.authentication.operation

import com.book.auth.domain.api.authentication.entity.AssertionStartResponse

interface StartAssertion {
    suspend operator fun invoke(): Result<AssertionStartResponse>
}
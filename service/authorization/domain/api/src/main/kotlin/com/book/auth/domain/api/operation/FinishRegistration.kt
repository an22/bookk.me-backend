package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.PasskeySignUpInfo
import com.book.core.domain.operation.SuspendOperation

interface FinishRegistration : SuspendOperation<PasskeySignUpInfo, Result<Unit>>
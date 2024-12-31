package com.book.auth.domain.impl.totp

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import java.util.concurrent.TimeUnit

internal fun createTotpConfig() = TimeBasedOneTimePasswordConfig(
    codeDigits = 6,
    hmacAlgorithm = HmacAlgorithm.SHA1,
    timeStep = 30,
    timeStepUnit = TimeUnit.SECONDS
)
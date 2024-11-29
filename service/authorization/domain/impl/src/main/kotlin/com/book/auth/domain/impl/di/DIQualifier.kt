package com.book.auth.domain.impl.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

enum class DIQualifier(override val value: QualifierValue) : Qualifier {
    AUTH("auth")
}
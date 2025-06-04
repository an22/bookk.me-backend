package com.book.business.domain.api.entity

import com.book.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.CurrencyUnit

@Serializable
class BusinessUpdateModel(
    val id: Long,
    val name: String?,
    val description: String?,
    val location: Business.Location?,
    @Serializable(with = MoneySerializer::class)
    val currencyUnit: CurrencyUnit?,
    val socials: List<Business.Social>?
)
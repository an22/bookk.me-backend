package com.bookk.business.data.map

import com.bookk.business.data.orm.entity.BusinessEntity
import com.bookk.business.domain.api.business.entity.Business
import kotlinx.datetime.TimeZone
import kotlin.uuid.toKotlinUuid

internal fun BusinessEntity.toDomain(): Business {
    return Business(
        id = id.value.toKotlinUuid(),
        name = name,
        description = description,
        location = if (latitude != null && longitude != null) {
            Business.Location(
                latitude ?: 0.0,
                longitude ?: 0.0
            )
        } else null,
        currencyCode = currency,
        address = address,
        timeZone = TimeZone.of(timezone),
        socials = listOf(
            Business.Social(
                Business.SocialKind.PHONE,
                phone.orEmpty()
            ),
            Business.Social(
                Business.SocialKind.INSTAGRAM,
                instagram.orEmpty()
            ),
            Business.Social(
                Business.SocialKind.TELEGRAM,
                telegram.orEmpty()
            ),
            Business.Social(
                Business.SocialKind.WHATSAPP,
                whatsapp.orEmpty()
            ),
            Business.Social(
                Business.SocialKind.VIBER,
                viber.orEmpty()
            )
        )
    )
}
package com.book.business.data.map

import com.book.business.data.orm.entity.BusinessEntity
import com.book.business.domain.api.entity.Business

internal fun BusinessEntity.toDomain(): Business {
    return Business(
        id = id.value,
        name = name
    )
}
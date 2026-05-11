package com.bookk.business.data.orm.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object ServiceTable : UUIDTable("service") {
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE).index()
    val groupId = reference("group_id", ServiceGroupTable, onDelete = ReferenceOption.CASCADE).index()
    val name = varchar("name", 512)
    val duration = integer("duration")
    val priceCurrency = varchar("price_currency", 3)
    val priceUnscaled = long("price_unscaled")
    val priceScale = integer("price_scale")

    init {
        index(true, businessId, groupId, name)
    }
}
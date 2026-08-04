package com.bookk.server.business.client.api.event

import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.business.client.api.BusinessDTO
import kotlinx.datetime.TimeZone
import kotlinx.serialization.ExperimentalSerializationApi
import library.schedule.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class BusinessEventTest {

    private val businessId = Uuid.random()

    private fun businessDto() = BusinessDTO(
        id = businessId,
        name = "Business name",
        address = "Business address",
        timeZone = TimeZone.UTC,
        schedule = Schedule()
    )

    @Test
    fun `should key the updated event by business id`() = runUnitTest {
        given()
        val event = BusinessEvent.Updated(businessDto(), Instant.fromEpochMilliseconds(1000))

        whenn()
        val partitionKey = event.partitionKey

        then()
        assertEquals(businessId.toString(), partitionKey)
    }

    @Test
    fun `should key the deleted event by business id`() = runUnitTest {
        given()
        val event = BusinessEvent.Deleted(businessId)

        whenn()
        val partitionKey = event.partitionKey

        then()
        assertEquals(businessId.toString(), partitionKey)
    }

    @Test
    fun `should route both business events for one business to the same partition key`() = runUnitTest {
        given()
        val updated = BusinessEvent.Updated(businessDto(), Instant.fromEpochMilliseconds(1000))
        val deleted = BusinessEvent.Deleted(businessId)

        whenn()
        val keys = setOf(updated.partitionKey, deleted.partitionKey)

        then()
        assertEquals(1, keys.size)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `should keep the partition key out of the serialized form`() = runUnitTest {
        given()
        val descriptor = BusinessEvent.Updated.serializer().descriptor

        whenn()
        val serializedFields = (0 until descriptor.elementsCount).map { descriptor.getElementName(it) }

        then()
        assertFalse(serializedFields.contains("partitionKey"))
        assertEquals(listOf("business", "updatedAt", "idempotencyKey", "topic"), serializedFields)
    }
}

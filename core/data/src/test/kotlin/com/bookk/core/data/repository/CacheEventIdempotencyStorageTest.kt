package com.bookk.core.data.repository

import com.bookk.core.data.cache.CacheClient
import com.bookk.core.data.cache.test.InMemoryCacheClient
import com.bookk.core.data.eventstreaming.EventClaim
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.uuid.Uuid

internal class CacheEventIdempotencyStorageTest {

    private class SutFixture {
        val sut = CacheEventIdempotencyStorage(InMemoryCacheClient())
        val topic = "topic.idempotency"
        val idempotencyKey = Uuid.random().toString()
    }

    private class KeyReleasedAfterFirstClaimAttemptCacheClient(
        private val delegate: CacheClient<String> = InMemoryCacheClient()
    ) : CacheClient<String> by delegate {
        private var firstAttempt = true

        override suspend fun <V : Any> setIfAbsent(key: String, value: V, kType: KType, expiration: Duration?): Boolean {
            if (firstAttempt) {
                firstAttempt = false
                return false
            }
            return delegate.setIfAbsent(key, value, kType, expiration)
        }
    }

    @Test
    fun `should claim an event whose claim was released between the claim attempt and the read`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val sut = CacheEventIdempotencyStorage(KeyReleasedAfterFirstClaimAttemptCacheClient())

        whenn()
        val claim = sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.CLAIMED, claim)
    }

    @Test
    fun `should claim an event the consumer group has not seen`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.CLAIMED, claim)
    }

    @Test
    fun `should report an event claimed by the same consumer group as in progress`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.IN_PROGRESS, claim)
    }

    @Test
    fun `should report an event marked as processed as already processed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)
        fixture.sut.markProcessed("business_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.ALREADY_PROCESSED, claim)
    }

    @Test
    fun `should keep an event processed after another claim attempt`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)
        fixture.sut.markProcessed("business_group", fixture.topic, fixture.idempotencyKey)
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.ALREADY_PROCESSED, claim)
    }

    @Test
    fun `should let an event be claimed again after its claim is released`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)
        fixture.sut.release("business_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.CLAIMED, claim)
    }

    @Test
    fun `should claim an event processed by another consumer group`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("user_group", fixture.topic, fixture.idempotencyKey)
        fixture.sut.markProcessed("user_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.CLAIMED, claim)
    }

    @Test
    fun `should claim the same idempotency key independently on another topic`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.sut.claim("business_group", fixture.topic, fixture.idempotencyKey)

        whenn()
        val claim = fixture.sut.claim("business_group", "topic.other", fixture.idempotencyKey)

        then()
        assertEquals(EventClaim.CLAIMED, claim)
    }
}

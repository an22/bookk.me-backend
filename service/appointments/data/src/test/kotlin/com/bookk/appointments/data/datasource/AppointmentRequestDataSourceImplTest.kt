package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.AppointmentRequestServicesTable
import com.bookk.appointments.data.orm.table.AppointmentRequestTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentRequestStatus
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.cache.test.InMemoryCacheClient
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class AppointmentRequestDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            AppointmentBusinessTable, WorkingHoursTable, DayOffsTable, AppointmentRequestTable, AppointmentRequestServicesTable
        )
        val cacheClient = InMemoryCacheClient()
        val sut = AppointmentRequestDataSourceImpl(cacheClient)
        val subscriptionSut = AppointmentSubscriptionDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            val snapshot = BusinessSnapshot.stub()
            suspendTransaction { subscriptionSut.attachBusiness(snapshot) }
            businessId = snapshot.id
        }

        fun buildRequest(
            userId: Uuid = Uuid.random(),
            date: Instant = Instant.fromEpochMilliseconds(0)
        ) = AppointmentRequest.stub(userId = userId, businessId = businessId, date = date)
    }

    @Test
    fun `should create request and retrieve by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val request = fixture.buildRequest()

        whenn()
        val created = suspendTransaction { fixture.sut.create(request) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found!!.id)
        assertEquals(AppointmentRequestStatus.PENDING, found.status)
    }

    @Test
    fun `should return null when request not found`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val found = suspendTransaction { fixture.sut.get(Uuid.random()) }

        then()
        assertNull(found)
    }

    @Test
    fun `should retrieve all requests for business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }

        then()
        assertEquals(2, all.size)
    }

    @Test
    fun `should retrieve only pending requests`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val pending = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.approve(pending) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val pending2 = suspendTransaction { fixture.sut.getPending(fixture.businessId) }

        then()
        assertEquals(1, pending2.size)
        assertEquals(AppointmentRequestStatus.PENDING, pending2.first().status)
    }

    @Test
    fun `should approve request`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.approve(created) }
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }

        then()
        assertEquals(AppointmentRequestStatus.APPROVED, all.first().status)
    }

    @Test
    fun `should decline request with reason`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val declined = suspendTransaction { fixture.sut.decline(created.id, "Fully booked") }

        then()
        assertEquals(AppointmentRequestStatus.DECLINED, declined.status)
        assertEquals("Fully booked", declined.declineReason)
    }

    @Test
    fun `should delete request`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.delete(created) }

        then()
        assertTrue(suspendTransaction { fixture.sut.getAll(fixture.businessId) }.isEmpty())
    }

    @Test
    fun `should detect overlapping requests`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val baseDate = Clock.System.now()
        suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId, date = baseDate)) }

        whenn()
        val overlapping = fixture.buildRequest(userId = userId, date = baseDate + 15.minutes)
        val hasOverlap = suspendTransaction { fixture.sut.hasOverlapsWith(overlapping) }

        then()
        assertTrue(hasOverlap)
    }

    @Test
    fun `should not detect overlap for distant requests`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val baseDate = Clock.System.now()
        suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId, date = baseDate)) }

        whenn()
        val nonOverlapping = fixture.buildRequest(userId = userId, date = baseDate + 2.hours)
        val hasOverlap = suspendTransaction { fixture.sut.hasOverlapsWith(nonOverlapping) }

        then()
        assertFalse(hasOverlap)
    }

    @Test
    fun `should update request`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val updated = suspendTransaction { fixture.sut.update(created.copy(note = "New note")) }

        then()
        assertEquals("New note", updated.note)
    }

    @Test
    fun `should cancel outdated pending requests before cutoff`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        // date at epoch → dateEnd = epoch + 30 min, well before now
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = Instant.fromEpochMilliseconds(0))) }

        whenn()
        suspendTransaction { fixture.sut.cancelOutdated(Clock.System.now()) }

        then()
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }
        assertEquals(AppointmentRequestStatus.CANCELLED, all.first { it.id == created.id }.status)
    }

    @Test
    fun `should not cancel future pending requests`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val futureDate = Clock.System.now() + 24.hours
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = futureDate)) }

        whenn()
        suspendTransaction { fixture.sut.cancelOutdated(Clock.System.now()) }

        then()
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }
        assertEquals(AppointmentRequestStatus.PENDING, all.first { it.id == created.id }.status)
    }

    @Test
    fun `should cache offer token and detect it in cache`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val token = "offer-token-abc"

        whenn()
        fixture.sut.cacheOfferToken(token)

        then()
        assertTrue(fixture.sut.isTokenInCache(token))
    }

    @Test
    fun `should return false from isTokenInCache for missing token`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val inCache = fixture.sut.isTokenInCache("nonexistent-token")

        then()
        assertFalse(inCache)
    }

    @Test
    fun `should delete requests booked with the deleted user as client`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val request = fixture.buildRequest().let {
            it.copy(client = it.client.copy(id = userId))
        }
        val created = suspendTransaction { fixture.sut.create(request) }

        whenn()
        suspendTransaction { fixture.sut.deleteForUser(userId) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should delete requests assigned to the deleted user as employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val request = fixture.buildRequest().let {
            it.copy(employee = it.employee.copy(id = userId))
        }
        val created = suspendTransaction { fixture.sut.create(request) }

        whenn()
        suspendTransaction { fixture.sut.deleteForUser(userId) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should not delete requests belonging to other users`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val untouched = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.deleteForUser(userId) }
        val found = suspendTransaction { fixture.sut.get(untouched.id) }

        then()
        assertNotNull(found)
    }

    @Test
    fun `should not fail deleting requests for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.deleteForUser(Uuid.random()) } }

        then()
        assertTrue(result.isSuccess)
    }
}

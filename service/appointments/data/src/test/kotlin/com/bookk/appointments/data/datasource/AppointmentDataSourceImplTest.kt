package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.AppointmentServicesTable
import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
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

internal class AppointmentDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(AppointmentBusinessTable, WorkingHoursTable, DayOffsTable, AppointmentTable, AppointmentServicesTable)
        val sut = AppointmentDataSourceImpl()
        val subscriptionSut = AppointmentSubscriptionDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            val snapshot = BusinessSnapshot.stub()
            suspendTransaction { subscriptionSut.attachBusiness(snapshot) }
            businessId = snapshot.id
        }

        fun buildRequest(userId: Uuid = Uuid.random(), date: Instant = Instant.fromEpochMilliseconds(0)) =
            AppointmentRequest.stub(userId = userId, businessId = businessId, date = date)
    }

    @Test
    fun `should create appointment from request and retrieve by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val request = fixture.buildRequest()

        whenn()
        val created = suspendTransaction { fixture.sut.create(request) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals(fixture.businessId, found.businessId)
        assertEquals(AppointmentStatus.SCHEDULED, found.status)
    }

    @Test
    fun `should create appointment from appointment entity`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val appointment = Appointment.stub(businessId = fixture.businessId)

        whenn()
        val created = suspendTransaction { fixture.sut.create(appointment) }

        then()
        assertNotNull(created)
        assertEquals(fixture.businessId, created.businessId)
    }

    @Test
    fun `should retrieve all appointments for business`() = runUnitTest {
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
    fun `should return empty list when no appointments exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }

        then()
        assertTrue(all.isEmpty())
    }

    @Test
    fun `should update appointment note`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val updated = suspendTransaction { fixture.sut.update(created.copy(note = "Updated note")) }

        then()
        assertEquals("Updated note", updated.note)
    }

    @Test
    fun `should cancel appointment with reason`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val cancelled = suspendTransaction { fixture.sut.cancel(created.id, "Client no-show") }

        then()
        assertEquals(AppointmentStatus.CANCELLED, cancelled.status)
        assertEquals("Client no-show", cancelled.cancellationReason)
    }

    @Test
    fun `should delete appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.delete(created.id) }

        then()
        assertTrue(suspendTransaction { fixture.sut.getAll(fixture.businessId) }.isEmpty())
    }

    @Test
    fun `should detect overlapping appointments for same user`() = runUnitTest {
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
    fun `should not detect overlap when appointments are far apart`() = runUnitTest {
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
    fun `should retrieve appointments for date range`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val baseDate = Instant.fromEpochMilliseconds(1_000_000_000_000L)
        suspendTransaction { fixture.sut.create(fixture.buildRequest(date = baseDate)) }

        whenn()
        val range = baseDate..(baseDate + 1.hours)
        val found = suspendTransaction { fixture.sut.getAllForDate(fixture.businessId, range) }

        then()
        assertEquals(1, found.size)
    }

    @Test
    fun `should return all appointments with pagination metadata`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val pagination = suspendTransaction { fixture.sut.getAllPaginated(fixture.businessId, 10, 0, null) }

        then()
        assertEquals(3, pagination.data.size)
        assertEquals(3L, pagination.metadata.total)
        assertEquals(1L, pagination.metadata.page)
        assertEquals(10, pagination.metadata.pageSize)
    }

    @Test
    fun `should filter paginated appointments by client name query`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        // ClientSnapshot.stub() uses fullName "Client Name"
        val pagination = suspendTransaction { fixture.sut.getAllPaginated(fixture.businessId, 10, 0, "client") }

        then()
        assertEquals(2, pagination.data.size)
    }

    @Test
    fun `should filter paginated appointments by service name query`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        // ServiceSnapshot.stub() uses name "Service Name"
        val pagination = suspendTransaction { fixture.sut.getAllPaginated(fixture.businessId, 10, 0, "service") }

        then()
        assertEquals(1, pagination.data.size)
    }

    @Test
    fun `should return empty list when paginated query matches nothing`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val pagination = suspendTransaction { fixture.sut.getAllPaginated(fixture.businessId, 10, 0, "xyz-nonexistent") }

        then()
        assertTrue(pagination.data.isEmpty())
        assertEquals(0L, pagination.metadata.total)
    }

    @Test
    fun `should paginate appointments using offset`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val page2 = suspendTransaction { fixture.sut.getAllPaginated(fixture.businessId, 2, 2, null) }

        then()
        assertEquals(1, page2.data.size)
        assertEquals(3L, page2.metadata.total)
        assertEquals(2L, page2.metadata.page)
    }

    @Test
    fun `should mark past scheduled appointments as completed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        // date at epoch → dateEnd = epoch + 30 min, well before now
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = Instant.fromEpochMilliseconds(0))) }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        val updated = suspendTransaction { fixture.sut.getAll(fixture.businessId) }
        assertEquals(AppointmentStatus.COMPLETED, updated.first { it.id == created.id }.status)
    }

    @Test
    fun `should not mark future appointments as completed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val futureDate = Clock.System.now() + 24.hours
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = futureDate)) }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        val all = suspendTransaction { fixture.sut.getAll(fixture.businessId) }
        assertEquals(AppointmentStatus.SCHEDULED, all.first { it.id == created.id }.status)
    }

    @Test
    fun `should anonymize client PII on appointments booked with the deleted user as client`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val request = fixture.buildRequest().let {
            it.copy(client = it.client.copy(id = userId, fullName = "Alice", phone = "+123", email = "alice@test.com"))
        }
        val created = suspendTransaction { fixture.sut.create(request) }

        whenn()
        suspendTransaction { fixture.sut.anonymizeForUser(userId) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertEquals("Deleted User", found.client.fullName)
        assertNull(found.client.phone)
        assertNull(found.client.email)
    }

    @Test
    fun `should anonymize employee name on appointments assigned to the deleted user as employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val request = fixture.buildRequest().let {
            it.copy(employee = it.employee.copy(userId = userId, fullName = "Bob"))
        }
        val created = suspendTransaction { fixture.sut.create(request) }

        whenn()
        suspendTransaction { fixture.sut.anonymizeForUser(userId) }
        val found = suspendTransaction { fixture.sut.get(created.id) }

        then()
        assertEquals("Deleted User", found.employee.fullName)
    }

    @Test
    fun `should not fail anonymizing appointments for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.anonymizeForUser(Uuid.random()) } }

        then()
        assertTrue(result.isSuccess)
    }
}

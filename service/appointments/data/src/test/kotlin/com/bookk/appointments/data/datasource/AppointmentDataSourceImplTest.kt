package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.AppointmentServicesTable
import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCompletedBy
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.domain.entity.Error
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

    private val eligibleStatuses = setOf(AppointmentStatus.SCHEDULED, AppointmentStatus.COMPLETED)

    private class SutFixture {
        val db = createTestDatabase(
            AppointmentBusinessTable, WorkingHoursTable, DayOffsTable, SettingsTable, AppointmentTable, AppointmentServicesTable
        )
        val sut = AppointmentDataSourceImpl()
        val subscriptionSut = AppointmentSubscriptionDataSourceImpl()
        val settingsSut = AppointmentSettingsDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup(automaticCompletion: Boolean = true) {
            businessId = attachBusiness(automaticCompletion)
        }

        suspend fun attachBusiness(automaticCompletion: Boolean): Uuid {
            val snapshot = BusinessSnapshot.stub()
            suspendTransaction {
                subscriptionSut.attachBusiness(snapshot)
                settingsSut.create(AppointmentSettings.stub(snapshot.id).copy(automaticCompletion = automaticCompletion))
            }
            return snapshot.id
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
    fun `should retrieve appointment for update by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val found = suspendTransaction { fixture.sut.getForUpdate(created.id) }

        then()
        assertEquals(created.id, found.id)
        assertEquals(AppointmentStatus.SCHEDULED, found.status)
    }

    @Test
    fun `should lock the appointment row when retrieving it for update`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        val capturedSql = CapturingSqlLogger()

        whenn()
        suspendTransaction {
            addLogger(capturedSql)
            fixture.sut.getForUpdate(created.id)
        }

        then()
        val selects = capturedSql.statements.filter { it.startsWith("SELECT", ignoreCase = true) }
        assertTrue(selects.any { it.contains("FOR UPDATE", ignoreCase = true) })
    }

    @Test
    fun `should throw not found when retrieving missing appointment for update`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.getForUpdate(Uuid.random()) } }

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
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
    fun `should ignore status when updating appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.update(created.copy(note = "Updated note", status = AppointmentStatus.COMPLETED))
        }

        then()
        assertEquals(AppointmentStatus.SCHEDULED, updated.status)
        assertEquals("Updated note", updated.note)
        assertEquals(AppointmentStatus.SCHEDULED, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should return the stored services after updating appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        val newServices = listOf(ServiceSnapshot.stub(), ServiceSnapshot.stub())

        whenn()
        val updated = suspendTransaction { fixture.sut.update(created.copy(services = newServices)) }

        then()
        assertEquals(newServices.map { it.id }.toSet(), updated.services.map { it.id }.toSet())
        assertEquals(newServices.map { it.id }.toSet(), suspendTransaction { fixture.sut.get(created.id) }.services.map { it.id }.toSet())
    }

    @Test
    fun `should return the latest services when updating twice in one transaction`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        val latestServices = listOf(ServiceSnapshot.stub(), ServiceSnapshot.stub())

        whenn()
        val updated = suspendTransaction {
            fixture.sut.update(created.copy(services = listOf(ServiceSnapshot.stub())))
            fixture.sut.update(created.copy(services = latestServices))
        }

        then()
        assertEquals(latestServices.map { it.id }.toSet(), updated.services.map { it.id }.toSet())
    }

    @Test
    fun `should throw not found when updating missing appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.update(Appointment.stub(businessId = fixture.businessId)) } }

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should create appointment at the start of a cancelled one for the same client`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val cancelled = suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId)) }
        suspendTransaction { fixture.sut.cancel(cancelled.id, "Reason") }

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId)) } }

        then()
        assertTrue(result.isSuccess)
        assertEquals(AppointmentStatus.SCHEDULED, result.getOrThrow().status)
    }

    @Test
    fun `should reschedule appointment to the start of a cancelled one for the same client`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val cancelledStart = Instant.fromEpochMilliseconds(0)
        val cancelled = suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId, date = cancelledStart)) }
        suspendTransaction { fixture.sut.cancel(cancelled.id, "Reason") }
        val scheduled = suspendTransaction { fixture.sut.create(fixture.buildRequest(userId = userId, date = cancelledStart + 2.hours)) }

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.update(scheduled.copy(date = cancelledStart)) } }

        then()
        assertTrue(result.isSuccess)
        assertEquals(cancelledStart, suspendTransaction { fixture.sut.get(scheduled.id) }.date)
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
    fun `should record the system as the completer of automatically completed appointments`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        assertEquals(AppointmentCompletedBy.SYSTEM, suspendTransaction { fixture.sut.get(created.id) }.completedBy)
    }

    @Test
    fun `should not mark past appointments as completed when the business disabled automatic completion`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup(automaticCompletion = false)
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        val found = suspendTransaction { fixture.sut.get(created.id) }
        assertEquals(AppointmentStatus.SCHEDULED, found.status)
        assertNull(found.completedBy)
    }

    @Test
    fun `should only mark appointments of businesses with automatic completion enabled`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup(automaticCompletion = true)
        val disabledBusinessId = fixture.attachBusiness(automaticCompletion = false)
        val enabledAppointment = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        val disabledAppointment = suspendTransaction {
            fixture.sut.create(AppointmentRequest.stub(businessId = disabledBusinessId, date = Instant.fromEpochMilliseconds(0)))
        }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.COMPLETED, suspendTransaction { fixture.sut.get(enabledAppointment.id) }.status)
        assertEquals(AppointmentStatus.SCHEDULED, suspendTransaction { fixture.sut.get(disabledAppointment.id) }.status)
    }

    @Test
    fun `should not create appointments as completed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        then()
        assertNull(suspendTransaction { fixture.sut.get(created.id) }.completedBy)
    }

    @Test
    fun `should mark started scheduled appointment as completed by user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup(automaticCompletion = false)
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val completed = suspendTransaction { fixture.sut.markCompletedByUser(created.id, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.COMPLETED, completed.status)
        assertEquals(AppointmentCompletedBy.USER, completed.completedBy)
        val found = suspendTransaction { fixture.sut.get(created.id) }
        assertEquals(AppointmentStatus.COMPLETED, found.status)
        assertEquals(AppointmentCompletedBy.USER, found.completedBy)
    }

    @Test
    fun `should not complete appointment by user when it has not started yet`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = Clock.System.now() + 24.hours)) }

        whenn()
        val result = suspendTransaction { fixture.sut.markCompletedByUser(created.id, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.SCHEDULED, result.status)
        assertNull(result.completedBy)
        assertEquals(AppointmentStatus.SCHEDULED, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should not complete cancelled appointment by user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.cancel(created.id, "Reason") }

        whenn()
        val result = suspendTransaction { fixture.sut.markCompletedByUser(created.id, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.CANCELLED, result.status)
        assertNull(result.completedBy)
    }

    @Test
    fun `should keep the system as completer when user completes an already completed appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        whenn()
        val result = suspendTransaction { fixture.sut.markCompletedByUser(created.id, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.COMPLETED, result.status)
        assertEquals(AppointmentCompletedBy.SYSTEM, result.completedBy)
    }

    @Test
    fun `should throw not found when user completes missing appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.markCompletedByUser(Uuid.random(), Clock.System.now()) } }

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
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
    fun `should not mark no-show appointments as completed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.markNoShow(created.id, eligibleStatuses, Clock.System.now()) }

        whenn()
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.NO_SHOW, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should mark started scheduled appointment as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }

        whenn()
        val marked = suspendTransaction { fixture.sut.markNoShow(created.id, eligibleStatuses, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.NO_SHOW, marked.status)
        assertEquals(AppointmentStatus.NO_SHOW, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should mark completed appointment as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        whenn()
        val marked = suspendTransaction { fixture.sut.markNoShow(created.id, eligibleStatuses, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.NO_SHOW, marked.status)
        assertNull(marked.completedBy)
    }

    @Test
    fun `should not mark appointment that has not started yet as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest(date = Clock.System.now() + 24.hours)) }

        whenn()
        val result = suspendTransaction { fixture.sut.markNoShow(created.id, eligibleStatuses, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.SCHEDULED, result.status)
        assertEquals(AppointmentStatus.SCHEDULED, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should not mark cancelled appointment as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.cancel(created.id, "Reason") }

        whenn()
        val result = suspendTransaction { fixture.sut.markNoShow(created.id, eligibleStatuses, Clock.System.now()) }

        then()
        assertEquals(AppointmentStatus.CANCELLED, result.status)
        assertEquals(AppointmentStatus.CANCELLED, suspendTransaction { fixture.sut.get(created.id) }.status)
    }

    @Test
    fun `should not mark appointment with status outside eligible statuses as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(fixture.buildRequest()) }
        suspendTransaction { fixture.sut.markCompleted(Clock.System.now()) }

        whenn()
        val result = suspendTransaction {
            fixture.sut.markNoShow(created.id, setOf(AppointmentStatus.SCHEDULED), Clock.System.now())
        }

        then()
        assertEquals(AppointmentStatus.COMPLETED, result.status)
    }

    @Test
    fun `should throw not found when marking missing appointment as no-show`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.markNoShow(Uuid.random(), eligibleStatuses, Clock.System.now()) } }

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
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

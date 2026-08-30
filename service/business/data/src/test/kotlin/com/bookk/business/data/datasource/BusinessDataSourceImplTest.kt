package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import library.schedule.DayOffRange
import library.schedule.Schedule
import library.schedule.WorkHour
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class BusinessDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable,
            BusinessDashboardTable,
            BusinessPermissionsTable,
            BusinessWorkingHoursTable,
            BusinessDayOffTable
        )
        val sut = BusinessDataSourceImpl()
    }

    private fun updateModel(
        id: Uuid,
        name: String? = null,
        schedule: Schedule? = null,
        dayOffs: List<DayOffRange> = emptyList()
    ) = BusinessUpdateModel(
        id = id,
        name = name,
        description = null,
        address = null,
        location = null,
        currencyCode = null,
        timeZone = null,
        socials = null,
        schedule = schedule?.copy(dayOffs = dayOffs)
    )

    @Test
    fun `should create business and retrieve by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()

        whenn()
        val created = suspendTransaction {
            fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC)
        }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertNotNull(found)
        assertEquals("Salon", found!!.name)
        assertEquals("USD", found.currencyCode)
    }

    @Test
    fun `should return null when business not found`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val found = suspendTransaction { fixture.sut.getBusinessById(Uuid.random()) }

        then()
        assertNull(found)
    }

    @Test
    fun `should report business exists after creation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val exists = suspendTransaction { fixture.sut.isBusinessExist(userId) }

        then()
        assertTrue(exists)
    }

    @Test
    fun `should report business does not exist for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val exists = suspendTransaction { fixture.sut.isBusinessExist(Uuid.random()) }

        then()
        assertFalse(exists)
    }

    @Test
    fun `should create business with default working schedule`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()

        whenn()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        then()
        assertEquals(
            DayOfWeek.entries.filter { it < DayOfWeek.SATURDAY },
            created.schedule.activeDays().sorted()
        )
        assertEquals(
            listOf(WorkHour(LocalTime(9, 0), LocalTime(17, 0))),
            created.schedule[DayOfWeek.MONDAY].workingTime
        )
        assertTrue(created.schedule.dayOffs.isEmpty())
    }

    @Test
    fun `should update business schedule and day offs`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }
        val schedule = Schedule(
            workingDays = listOf(DayOfWeek.SATURDAY),
            workingHours = mapOf(
                DayOfWeek.SATURDAY to listOf(WorkHour(LocalTime(10, 0), LocalTime(14, 0)))
            )
        )
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31)))

        whenn()
        suspendTransaction {
            fixture.sut.updateBusiness(updateModel(created.id, schedule = schedule, dayOffs = dayOffs), Clock.System.now())
        }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals(listOf(DayOfWeek.SATURDAY), found!!.schedule.activeDays())
        assertEquals(LocalTime(10, 0), found.schedule[DayOfWeek.SATURDAY].workingTime.single().from)
        assertEquals(LocalTime(14, 0), found.schedule[DayOfWeek.SATURDAY].workingTime.single().to)
        assertEquals(dayOffs, found.schedule.dayOffs)
    }

    @Test
    fun `should persist working hours under the day they are keyed under`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }
        val schedule = Schedule(
            workingDays = listOf(DayOfWeek.MONDAY),
            workingHours = mapOf(DayOfWeek.MONDAY to listOf(WorkHour(LocalTime(10, 0), LocalTime(14, 0))))
        )

        whenn()
        suspendTransaction { fixture.sut.updateBusiness(updateModel(created.id, schedule = schedule), Clock.System.now()) }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals(listOf(DayOfWeek.MONDAY), found!!.schedule.activeDays())
        assertEquals(LocalTime(10, 0), found.schedule[DayOfWeek.MONDAY].workingTime.single().from)
        assertTrue(found.schedule[DayOfWeek.FRIDAY].workingTime.isEmpty())
    }

    @Test
    fun `should persist the supplied updated at on the business row`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }
        val updatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000)

        whenn()
        suspendTransaction { fixture.sut.updateBusiness(updateModel(created.id, name = "New name"), updatedAt) }
        val stored = suspendTransaction {
            BusinessTable.select(BusinessTable.updatedAt)
                .where { BusinessTable.id eq created.id }
                .single()[BusinessTable.updatedAt]
        }

        then()
        assertEquals(updatedAt, stored)
    }

    @Test
    fun `should keep schedule when update does not contain it`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }

        whenn()
        suspendTransaction { fixture.sut.updateBusiness(updateModel(created.id, name = "New name"), Clock.System.now()) }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals("New name", found!!.name)
        assertEquals(created.schedule.activeDays().sorted(), found.schedule.activeDays().sorted())
        assertEquals(
            created.schedule[DayOfWeek.MONDAY].workingTime,
            found.schedule[DayOfWeek.MONDAY].workingTime
        )
    }

    @Test
    fun `should delete day offs in the past`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }
        val future = DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31))
        suspendTransaction {
            fixture.sut.updateBusiness(
                updateModel(
                    created.id,
                    schedule = created.schedule,
                    dayOffs = listOf(DayOffRange(LocalDate(2020, 1, 1), LocalDate(2020, 1, 2)), future)
                ),
                Clock.System.now()
            )
        }

        whenn()
        suspendTransaction { fixture.sut.deleteDayOffsInThePast() }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals(listOf(future), found!!.schedule.dayOffs)
    }

    @Test
    fun `should retrieve dashboard business for user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Dashboard Salon", "USD", TimeZone.UTC) }

        whenn()
        val dashboard = suspendTransaction { fixture.sut.getDashboardBusiness(userId) }

        then()
        assertNotNull(dashboard)
        assertEquals(created.id, dashboard!!.id)
    }

    @Test
    fun `should return null dashboard for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val dashboard = suspendTransaction { fixture.sut.getDashboardBusiness(Uuid.random()) }

        then()
        assertNull(dashboard)
    }

    @Test
    fun `should get user businesses with dashboard id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val result = suspendTransaction { fixture.sut.getUserBusinesses(userId) }

        then()
        assertEquals(1, result.businesses.size)
        assertEquals(created.id, result.dashboardId)
    }

    @Test
    fun `should set and retrieve user permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        suspendTransaction { fixture.sut.setUserPermissions(userId, created.id, 7) }
        val permission = suspendTransaction { fixture.sut.getPermission(userId, created.id) }

        then()
        assertEquals(7, permission)
    }

    @Test
    fun `should return null permission when not set`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val permission = suspendTransaction { fixture.sut.getPermission(Uuid.random(), created.id) }

        then()
        assertNull(permission)
    }

    @Test
    fun `should delete user permissions across businesses`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val ownBusiness = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        val otherBusiness = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Other Salon", "USD", TimeZone.UTC) }
        suspendTransaction {
            fixture.sut.setUserPermissions(userId, ownBusiness.id, 7)
            fixture.sut.setUserPermissions(userId, otherBusiness.id, 1)
        }

        whenn()
        suspendTransaction { fixture.sut.deleteUserPermissions(userId) }

        then()
        assertNull(suspendTransaction { fixture.sut.getPermission(userId, ownBusiness.id) })
        assertNull(suspendTransaction { fixture.sut.getPermission(userId, otherBusiness.id) })
    }

    @Test
    fun `should not fail deleting permissions for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.deleteUserPermissions(Uuid.random()) } }

        then()
        assertTrue(result.isSuccess)
    }

    // deleteUserBusinesses uses deleteReturning which is not supported by Exposed's H2 dialect
}

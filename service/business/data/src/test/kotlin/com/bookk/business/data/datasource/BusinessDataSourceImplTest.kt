package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.DayOffRange
import com.bookk.business.domain.api.business.entity.ScheduleUpdate
import com.bookk.business.domain.api.business.entity.WorkHour
import com.bookk.business.domain.api.business.entity.WorkingSchedule
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
        schedule: WorkingSchedule? = null,
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
        schedule = schedule?.let { ScheduleUpdate(workingSchedule = it, dayOffs = dayOffs) }
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
            listOf(WorkHour(DayOfWeek.MONDAY, LocalTime(9, 0), LocalTime(17, 0))),
            created.schedule[DayOfWeek.MONDAY].workingTime
        )
        assertTrue(created.dayOffs.isEmpty())
    }

    @Test
    fun `should update business schedule and day offs`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }
        val schedule = WorkingSchedule(
            workingDays = listOf(DayOfWeek.SATURDAY),
            workingHours = mapOf(
                DayOfWeek.SATURDAY to listOf(WorkHour(DayOfWeek.SATURDAY, LocalTime(10, 0), LocalTime(14, 0)))
            )
        )
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31)))

        whenn()
        suspendTransaction {
            fixture.sut.updateBusiness(updateModel(created.id, schedule = schedule, dayOffs = dayOffs))
        }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals(listOf(DayOfWeek.SATURDAY), found!!.schedule.activeDays())
        assertEquals(LocalTime(10, 0), found.schedule[DayOfWeek.SATURDAY].workingTime.single().from)
        assertEquals(LocalTime(14, 0), found.schedule[DayOfWeek.SATURDAY].workingTime.single().to)
        assertEquals(dayOffs, found.dayOffs)
    }

    @Test
    fun `should keep schedule when update does not contain it`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC) }

        whenn()
        suspendTransaction { fixture.sut.updateBusiness(updateModel(created.id, name = "New name")) }
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
                )
            )
        }

        whenn()
        suspendTransaction { fixture.sut.deleteDayOffsInThePast() }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertEquals(listOf(future), found!!.dayOffs)
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

    // deleteUserBusinesses uses deleteReturning which is not supported by Exposed's H2 dialect
}

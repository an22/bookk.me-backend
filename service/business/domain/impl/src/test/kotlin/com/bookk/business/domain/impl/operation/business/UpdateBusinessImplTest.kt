package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.DayOffRange
import com.bookk.business.domain.api.business.entity.ScheduleUpdate
import com.bookk.business.domain.api.business.entity.WorkHour
import com.bookk.business.domain.api.business.entity.WorkingSchedule
import com.bookk.business.domain.api.business.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.business.client.api.event.BusinessEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UpdateBusinessImplTest {

    private val requestUserId = Uuid.random()

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>(relaxed = true)
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = UpdateBusinessImpl(businessDataSource, transactionManager, eventProducer)

        init {
            coEvery { businessDataSource.getPermission(any(), any()) } returns ObjectPermission.OWNER.int
        }

        fun grantPermission(permission: ObjectPermission?) {
            coEvery { businessDataSource.getPermission(any(), any()) } returns permission?.int
        }
    }

    private fun updateModel(
        id: Uuid = Uuid.random(),
        name: String? = null,
        description: String? = null,
        address: String? = null,
        location: Business.Location? = null,
        currencyCode: String? = null,
        timeZone: TimeZone? = null,
        socials: List<Business.Social>? = null,
        schedule: WorkingSchedule? = null,
        dayOffs: List<DayOffRange> = emptyList()
    ) = BusinessUpdateModel(
        id = id,
        name = name,
        description = description,
        address = address,
        location = location,
        currencyCode = currencyCode,
        timeZone = timeZone,
        socials = socials,
        schedule = schedule?.let { ScheduleUpdate(workingSchedule = it, dayOffs = dayOffs) }
    )

    private fun scheduleOf(vararg days: DayOfWeek) = WorkingSchedule(
        workingDays = days.toList(),
        workingHours = days.associateWith { listOf(WorkHour(it, LocalTime(9, 0), LocalTime(17, 0))) }
    )

    @Test
    fun `should return success when truncated values are provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val updateModel = updateModel(
            id = businessId,
            name = "A".repeat(1000),
            description = "D".repeat(2000),
            address = "Addr".repeat(200),
            currencyCode = "USDD",
            timeZone = TimeZone.UTC,
            socials = listOf(Business.Social(Business.SocialKind.INSTAGRAM, "V".repeat(500)))
        )

        whenn()
        val result = fixture.sut(requestUserId, updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.updateBusiness(match {
                it.name?.length == Business.MAX_NAME_LENGTH &&
                it.description?.length == Business.MAX_DESCRIPTION_LENGTH &&
                it.address?.length == Business.MAX_ADDRESS_LENGTH &&
                it.currencyCode?.length == Business.MAX_CURRENCY_CODE &&
                it.socials?.firstOrNull()?.value?.length == Business.MAX_SOCIAL_LENGTH
            })
        }
    }

    @Test
    fun `should return success when null values are provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val updateModel = updateModel(socials = emptyList())

        whenn()
        val result = fixture.sut(requestUserId, updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.updateBusiness(match {
                it.name == null && it.description == null && it.address == null && it.socials?.isEmpty() == true
            })
        }
    }

    @Test
    fun `should publish business updated event with the updated business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val schedule = scheduleOf(DayOfWeek.MONDAY)
        val updatedBusiness = Business.stub(
            id = businessId,
            name = "New Name",
            address = "New Address",
            schedule = schedule
        )
        coEvery { fixture.businessDataSource.updateBusiness(any()) } returns updatedBusiness
        val updateModel = updateModel(
            id = businessId,
            name = "New Name",
            address = "New Address",
            timeZone = TimeZone.UTC,
            socials = emptyList()
        )

        whenn()
        val result = fixture.sut(requestUserId, updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(match<BusinessEvent.Updated> {
                it.business.id == updatedBusiness.id &&
                it.business.name == updatedBusiness.name &&
                it.business.address == updatedBusiness.address &&
                it.business.timeZone == updatedBusiness.timeZone
            }, any())
        }
    }

    @Test
    fun `should return failure when active day has no work hours`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val schedule = WorkingSchedule(
            workingDays = listOf(DayOfWeek.MONDAY),
            workingHours = emptyMap()
        )

        whenn()
        val result = fixture.sut(requestUserId, updateModel(schedule = schedule))

        then()
        assertTrue(result.exceptionOrNull() is UpdateBusiness.Error.ActiveDayWithoutWorkHours)
    }

    @Test
    fun `should return failure when day off range start date is not before end date`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 31), LocalDate(2099, 12, 30)))

        whenn()
        val result = fixture.sut(requestUserId, updateModel(schedule = scheduleOf(DayOfWeek.MONDAY), dayOffs = dayOffs))

        then()
        assertTrue(result.exceptionOrNull() is UpdateBusiness.Error.InvalidDayOffRange)
    }

    @Test
    fun `should publish the updated schedule and day offs on the updated event`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val schedule = scheduleOf(DayOfWeek.SATURDAY)
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31)))
        val updatedBusiness = Business.stub(id = businessId, schedule = schedule, dayOffs = dayOffs)
        coEvery { fixture.businessDataSource.updateBusiness(any()) } returns updatedBusiness

        whenn()
        val result = fixture.sut(requestUserId, updateModel(id = businessId, schedule = schedule, dayOffs = dayOffs))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(match<BusinessEvent.Updated> {
                it.business.id == businessId &&
                it.business.schedule.workingDays == listOf(DayOfWeek.SATURDAY) &&
                it.business.schedule.workingHours == listOf(
                    BusinessEvent.WorkHourDTO(DayOfWeek.SATURDAY, LocalTime(9, 0), LocalTime(17, 0))
                ) &&
                it.business.schedule.dayOffs == listOf(
                    BusinessEvent.DayOffDTO(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31))
                )
            }, any())
        }
    }

    @Test
    fun `should publish the stored schedule when the update does not change it`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val schedule = scheduleOf(DayOfWeek.MONDAY)
        coEvery { fixture.businessDataSource.updateBusiness(any()) } returns
            Business.stub(id = businessId, schedule = schedule)

        whenn()
        val result = fixture.sut(requestUserId, updateModel(id = businessId, name = "New Name"))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(match<BusinessEvent.Updated> {
                it.business.schedule.workingDays == listOf(DayOfWeek.MONDAY)
            }, any())
        }
    }

    @Test
    fun `should publish exactly one event per update`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val schedule = scheduleOf(DayOfWeek.MONDAY)
        coEvery { fixture.businessDataSource.updateBusiness(any()) } returns
            Business.stub(id = businessId, schedule = schedule)

        whenn()
        val result = fixture.sut(requestUserId, updateModel(id = businessId, schedule = schedule))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.eventProducer.send(any(BusinessEvent.Updated::class), any()) }
    }

    @Test
    fun `should return failure when user has no edit permission for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(ObjectPermission.READ)

        whenn()
        val result = fixture.sut(requestUserId, updateModel(name = "New Name"))

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return failure when user has no permission record for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(null)

        whenn()
        val result = fixture.sut(requestUserId, updateModel(name = "New Name"))

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should not update business or publish event when user has no edit permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(ObjectPermission.READ)

        whenn()
        val result = fixture.sut(requestUserId, updateModel(name = "New Name", schedule = scheduleOf(DayOfWeek.MONDAY)))

        then()
        assertEquals(false, result.isSuccess)
        coVerify(exactly = 0) { fixture.businessDataSource.updateBusiness(any()) }
        coVerify(exactly = 0) { fixture.eventProducer.send(any(BusinessEvent.Updated::class), any()) }
    }

    @Test
    fun `should assert edit permission against the business from the update model`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()

        whenn()
        val result = fixture.sut(requestUserId, updateModel(id = businessId, name = "New Name"))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.businessDataSource.getPermission(requestUserId, businessId) }
    }

    @Test
    fun `should not update business when schedule is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val schedule = WorkingSchedule(workingDays = listOf(DayOfWeek.MONDAY), workingHours = emptyMap())

        whenn()
        val result = fixture.sut(requestUserId, updateModel(schedule = schedule))

        then()
        assertEquals(false, result.isSuccess)
        coVerify(exactly = 0) { fixture.businessDataSource.updateBusiness(any()) }
    }
}

package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class ServiceDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable, BusinessDashboardTable, BusinessPermissionsTable, BusinessWorkingHoursTable, BusinessDayOffTable,
            ServiceGroupTable, ServiceTable
        )
        val sut = ServiceDataSourceImpl()
        val businessSut = BusinessDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            val userId = Uuid.random()
            businessId = suspendTransaction {
                businessSut.createBusiness(userId, "Test Business", "USD", TimeZone.UTC)
            }.id
        }
    }

    @Test
    fun `should create service group and retrieve`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = ServiceGroup.stub(businessId = fixture.businessId)

        whenn()
        val created = suspendTransaction { fixture.sut.createServiceGroup(group) }
        val groups = suspendTransaction { fixture.sut.getServiceGroups(fixture.businessId) }

        then()
        assertEquals(1, groups.size)
        assertEquals(group.name, groups.first().name)
        assertEquals(fixture.businessId, created.businessId)
    }

    @Test
    fun `should return empty list when no groups exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val groups = suspendTransaction { fixture.sut.getServiceGroups(fixture.businessId) }

        then()
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should create service and retrieve by business id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = suspendTransaction {
            fixture.sut.createServiceGroup(ServiceGroup.stub(businessId = fixture.businessId))
        }
        val service = Service.stub(businessId = fixture.businessId, group = group)

        whenn()
        val created = suspendTransaction { fixture.sut.createService(service) }
        val services = suspendTransaction { fixture.sut.getServices(fixture.businessId) }

        then()
        assertEquals(1, services.size)
        assertEquals(service.name, services.first().name)
        assertEquals(fixture.businessId, created.businessId)
    }

    @Test
    fun `should return empty list when no services exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val services = suspendTransaction { fixture.sut.getServices(fixture.businessId) }

        then()
        assertTrue(services.isEmpty())
    }

    @Test
    fun `should retrieve services by ids`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = suspendTransaction {
            fixture.sut.createServiceGroup(ServiceGroup.stub(businessId = fixture.businessId))
        }
        val created = suspendTransaction {
            fixture.sut.createService(Service.stub(businessId = fixture.businessId, group = group))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getServicesByIds(listOf(created.id)) }

        then()
        assertEquals(1, found.size)
        assertEquals(created.id, found.first().id)
    }

    @Test
    fun `should edit service and persist changes`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = suspendTransaction {
            fixture.sut.createServiceGroup(ServiceGroup.stub(businessId = fixture.businessId))
        }
        val created = suspendTransaction {
            fixture.sut.createService(Service.stub(businessId = fixture.businessId, group = group, isAvailable = true))
        }

        whenn()
        suspendTransaction { fixture.sut.editService(created.copy(isAvailable = false)) }
        val services = suspendTransaction { fixture.sut.getServices(fixture.businessId) }

        then()
        assertEquals(false, services.first().isAvailable)
    }

    @Test
    fun `should delete service`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = suspendTransaction {
            fixture.sut.createServiceGroup(ServiceGroup.stub(businessId = fixture.businessId))
        }
        val created = suspendTransaction {
            fixture.sut.createService(Service.stub(businessId = fixture.businessId, group = group))
        }

        whenn()
        suspendTransaction { fixture.sut.deleteService(created.id) }
        val services = suspendTransaction { fixture.sut.getServices(fixture.businessId) }

        then()
        assertTrue(services.isEmpty())
    }

    @Test
    fun `should delete service group`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val group = suspendTransaction {
            fixture.sut.createServiceGroup(ServiceGroup.stub(businessId = fixture.businessId))
        }

        whenn()
        suspendTransaction { fixture.sut.deleteServiceGroup(group.id) }
        val groups = suspendTransaction { fixture.sut.getServiceGroups(fixture.businessId) }

        then()
        assertTrue(groups.isEmpty())
    }
}

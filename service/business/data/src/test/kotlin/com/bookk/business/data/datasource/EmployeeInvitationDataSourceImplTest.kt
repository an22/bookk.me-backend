package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.EmployeeInvitationTable
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EmployeeInvitationDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable, BusinessDashboardTable, BusinessPermissionsTable, EmployeeInvitationTable
        )
        val sut = EmployeeInvitationDataSourceImpl()
        val businessSut = BusinessDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            businessId = suspendTransaction {
                businessSut.createBusiness(Uuid.random(), "Test Business", "USD", TimeZone.UTC)
            }.id
        }
    }

    @Test
    fun `should create invitation in pending status`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val invitedBy = Uuid.random()
        val invitation = EmployeeInvitation.stub(
            businessId = fixture.businessId,
            userId = userId,
            invitedBy = invitedBy,
            name = "Alice",
            lastName = "Smith",
            phone = "+1234567890",
            email = "alice@test.com"
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createInvitation(invitation) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(fixture.businessId, created.businessId)
        assertEquals(userId, created.userId)
        assertEquals(EmployeeInvitationStatus.PENDING, created.status)
        assertEquals(invitedBy, found?.invitedBy)
        assertEquals(userId, found?.userId)
    }

    @Test
    fun `should return invitation by business and id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, name = "Bob", lastName = "Brown")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals("Bob", found?.name)
        assertEquals("Brown", found?.lastName)
        assertEquals(EmployeeInvitationStatus.PENDING, found?.status)
    }

    @Test
    fun `should return null when invitation belongs to another business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitation(Uuid.random(), created.id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should approve pending invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        val approved = suspendTransaction { fixture.sut.approveInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertTrue(approved)
        assertEquals(EmployeeInvitationStatus.APPROVED, found?.status)
    }

    @Test
    fun `should not approve invitation twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.approveInvitation(created.id) }

        whenn()
        val secondApproval = suspendTransaction { fixture.sut.approveInvitation(created.id) }

        then()
        assertFalse(secondApproval)
    }

    @Test
    fun `should return false when approving unknown invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val approved = suspendTransaction { fixture.sut.approveInvitation(Uuid.random()) }

        then()
        assertFalse(approved)
    }

    @Test
    fun `should fail when inviting the same user to the same business twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, userId = userId))
        }

        whenn()
        val result = runCatching {
            suspendTransaction {
                fixture.sut.createInvitation(
                    EmployeeInvitation.stub(businessId = fixture.businessId, userId = userId)
                )
            }
        }

        then()
        assertTrue(result.exceptionOrNull() is Error.UniqueConstraintFailed)
    }
}

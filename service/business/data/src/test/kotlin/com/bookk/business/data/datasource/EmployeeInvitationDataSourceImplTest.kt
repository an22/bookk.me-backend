package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class EmployeeInvitationDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable, BusinessDashboardTable, BusinessPermissionsTable, BusinessWorkingHoursTable, BusinessDayOffTable, EmployeeInvitationTable
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
        val invitedBy = Uuid.random()
        val invitation = EmployeeInvitation.stub(
            businessId = fixture.businessId,
            invitedBy = invitedBy,
            email = "alice@test.com"
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createInvitation(invitation) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(fixture.businessId, created.businessId)
        assertEquals("alice@test.com", created.email)
        assertEquals(EmployeeInvitationStatus.PENDING, created.status)
        assertEquals(invitedBy, found?.invitedBy)
        assertEquals("alice@test.com", found?.email)
    }

    @Test
    fun `should return invitation by business and id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, email = "bob@test.com")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals("bob@test.com", found?.email)
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
    fun `should return invitations matching business and inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val invitedBy = Uuid.random()
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, email = "alice@test.com")
            )
        }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = Uuid.random(), email = "bob@test.com")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, invitedBy) }

        then()
        assertEquals(1, found.size)
        assertEquals("alice@test.com", found.first().email)
    }

    @Test
    fun `should return invitations by inviter regardless of status`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val invitedBy = Uuid.random()
        val approved = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, email = "alice@test.com")
            )
        }
        suspendTransaction { fixture.sut.approveInvitation(approved.id) }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, email = "bob@test.com")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, invitedBy) }

        then()
        assertEquals(2, found.size)
        assertTrue(found.any { it.email == "alice@test.com" && it.status == EmployeeInvitationStatus.APPROVED })
        assertTrue(found.any { it.email == "bob@test.com" && it.status == EmployeeInvitationStatus.PENDING })
    }

    @Test
    fun `should not return invitations from another inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = Uuid.random(), email = "alice@test.com")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, Uuid.random()) }

        then()
        assertTrue(found.isEmpty())
    }

    @Test
    fun `should return pending invitations matching email across businesses`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val otherBusinessId = suspendTransaction {
            fixture.businessSut.createBusiness(Uuid.random(), "Other Business", "USD", TimeZone.UTC)
        }.id
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, email = "alice@test.com")
            )
        }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = otherBusinessId, email = "alice@test.com")
            )
        }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, email = "bob@test.com")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getPendingInvitationsByEmail("alice@test.com") }

        then()
        assertEquals(2, found.size)
        assertTrue(found.all { it.email == "alice@test.com" })
    }

    @Test
    fun `should not return approved invitations as pending by email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, email = "alice@test.com")
            )
        }
        suspendTransaction { fixture.sut.approveInvitation(created.id) }

        whenn()
        val found = suspendTransaction { fixture.sut.getPendingInvitationsByEmail("alice@test.com") }

        then()
        assertTrue(found.isEmpty())
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
    fun `should reject pending invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        val rejected = suspendTransaction { fixture.sut.rejectInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertTrue(rejected)
        assertEquals(EmployeeInvitationStatus.REJECTED, found?.status)
    }

    @Test
    fun `should not reject invitation twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.rejectInvitation(created.id) }

        whenn()
        val secondRejection = suspendTransaction { fixture.sut.rejectInvitation(created.id) }

        then()
        assertFalse(secondRejection)
    }

    @Test
    fun `should not reject an already approved invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.approveInvitation(created.id) }

        whenn()
        val rejected = suspendTransaction { fixture.sut.rejectInvitation(created.id) }

        then()
        assertFalse(rejected)
    }

    @Test
    fun `should return false when rejecting unknown invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val rejected = suspendTransaction { fixture.sut.rejectInvitation(Uuid.random()) }

        then()
        assertFalse(rejected)
    }

    @Test
    fun `should revoke pending invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        val revoked = suspendTransaction { fixture.sut.revokeInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertTrue(revoked)
        assertEquals(EmployeeInvitationStatus.REVOKED, found?.status)
    }

    @Test
    fun `should not revoke invitation twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.revokeInvitation(created.id) }

        whenn()
        val secondRevocation = suspendTransaction { fixture.sut.revokeInvitation(created.id) }

        then()
        assertFalse(secondRevocation)
    }

    @Test
    fun `should not revoke an already approved invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.approveInvitation(created.id) }

        whenn()
        val revoked = suspendTransaction { fixture.sut.revokeInvitation(created.id) }

        then()
        assertFalse(revoked)
    }

    @Test
    fun `should return false when revoking unknown invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val revoked = suspendTransaction { fixture.sut.revokeInvitation(Uuid.random()) }

        then()
        assertFalse(revoked)
    }

    @Test
    fun `should expire pending invitations created before the cutoff`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        suspendTransaction { fixture.sut.expireOldInvitations(Clock.System.now().plus(1.hours)) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.EXPIRED, found?.status)
    }

    @Test
    fun `should not expire invitations created after the cutoff`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        suspendTransaction { fixture.sut.expireOldInvitations(Instant.fromEpochMilliseconds(0)) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.PENDING, found?.status)
    }

    @Test
    fun `should not expire invitations that are already approved`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.approveInvitation(created.id) }

        whenn()
        suspendTransaction { fixture.sut.expireOldInvitations(Clock.System.now().plus(1.hours)) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.APPROVED, found?.status)
    }

    @Test
    fun `should fail when inviting the same email to the same business twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, email = "alice@test.com"))
        }

        whenn()
        val result = runCatching {
            suspendTransaction {
                fixture.sut.createInvitation(
                    EmployeeInvitation.stub(businessId = fixture.businessId, email = "alice@test.com")
                )
            }
        }

        then()
        assertTrue(result.exceptionOrNull() is Error.UniqueConstraintFailed)
    }
}

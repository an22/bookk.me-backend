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
            code = "AAAA1111"
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createInvitation(invitation) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(fixture.businessId, created.businessId)
        assertEquals("AAAA1111", created.code)
        assertEquals(EmployeeInvitationStatus.PENDING, created.status)
        assertEquals(invitedBy, found?.invitedBy)
        assertEquals("AAAA1111", found?.code)
    }

    @Test
    fun `should return invitation by business and id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, code = "BBBB2222")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals("BBBB2222", found?.code)
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
    fun `should return invitation by code`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, code = "CCCC3333")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationByCode("CCCC3333") }

        then()
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals(fixture.businessId, found?.businessId)
    }

    @Test
    fun `should return null when code does not match any invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "DDDD4444"))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationByCode("UNKNOWN1") }

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
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, code = "AAAA1111")
            )
        }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = Uuid.random(), code = "BBBB2222")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, invitedBy) }

        then()
        assertEquals(1, found.size)
        assertEquals("AAAA1111", found.first().code)
    }

    @Test
    fun `should return invitations by inviter regardless of status`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val invitedBy = Uuid.random()
        val redeemed = suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, code = "AAAA1111")
            )
        }
        suspendTransaction { fixture.sut.redeemInvitation(redeemed.id) }
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = invitedBy, code = "BBBB2222")
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, invitedBy) }

        then()
        assertEquals(2, found.size)
        assertTrue(found.any { it.code == null && it.status == EmployeeInvitationStatus.REDEEMED })
        assertTrue(found.any { it.code == "BBBB2222" && it.status == EmployeeInvitationStatus.PENDING })
    }

    @Test
    fun `should not return invitations from another inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction {
            fixture.sut.createInvitation(
                EmployeeInvitation.stub(businessId = fixture.businessId, invitedBy = Uuid.random())
            )
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationsByInviter(fixture.businessId, Uuid.random()) }

        then()
        assertTrue(found.isEmpty())
    }

    @Test
    fun `should redeem pending invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }

        whenn()
        val redeemed = suspendTransaction { fixture.sut.redeemInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertTrue(redeemed)
        assertEquals(EmployeeInvitationStatus.REDEEMED, found?.status)
    }

    @Test
    fun `should not redeem invitation twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }

        whenn()
        val secondRedemption = suspendTransaction { fixture.sut.redeemInvitation(created.id) }

        then()
        assertFalse(secondRedemption)
    }

    @Test
    fun `should return false when redeeming unknown invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val redeemed = suspendTransaction { fixture.sut.redeemInvitation(Uuid.random()) }

        then()
        assertFalse(redeemed)
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
    fun `should not revoke an already redeemed invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }

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
    fun `should not expire invitations that are already redeemed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId))
        }
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }

        whenn()
        suspendTransaction { fixture.sut.expireOldInvitations(Clock.System.now().plus(1.hours)) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.REDEEMED, found?.status)
    }

    @Test
    fun `should clear the code when redeeming an invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "REDEEM01"))
        }

        whenn()
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.REDEEMED, found?.status)
        assertNull(found?.code)
    }

    @Test
    fun `should clear the code when revoking an invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "REVOKE01"))
        }

        whenn()
        suspendTransaction { fixture.sut.revokeInvitation(created.id) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.REVOKED, found?.status)
        assertNull(found?.code)
    }

    @Test
    fun `should clear the code when expiring an invitation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "EXPIRE01"))
        }

        whenn()
        suspendTransaction { fixture.sut.expireOldInvitations(Clock.System.now().plus(1.hours)) }
        val found = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, created.id) }

        then()
        assertEquals(EmployeeInvitationStatus.EXPIRED, found?.status)
        assertNull(found?.code)
    }

    @Test
    fun `should no longer find a redeemed invitation by its old code`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "STALE001"))
        }
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }

        whenn()
        val found = suspendTransaction { fixture.sut.getInvitationByCode("STALE001") }

        then()
        assertNull(found)
    }

    @Test
    fun `should allow reusing a code once its previous invitation is redeemed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "REUSE001"))
        }
        suspendTransaction { fixture.sut.redeemInvitation(created.id) }

        whenn()
        val recreated = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "REUSE001"))
        }

        then()
        assertEquals("REUSE001", recreated.code)
        assertEquals(EmployeeInvitationStatus.PENDING, recreated.status)
    }

    @Test
    fun `should allow two different processed invitations to both have a null code`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val first = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "FIRST001"))
        }
        val second = suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "SECOND01"))
        }

        whenn()
        suspendTransaction { fixture.sut.redeemInvitation(first.id) }
        val revoked = suspendTransaction { fixture.sut.revokeInvitation(second.id) }

        then()
        assertTrue(revoked)
        val foundFirst = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, first.id) }
        val foundSecond = suspendTransaction { fixture.sut.getInvitation(fixture.businessId, second.id) }
        assertNull(foundFirst?.code)
        assertNull(foundSecond?.code)
    }

    @Test
    fun `should fail when creating two invitations with the same code`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction {
            fixture.sut.createInvitation(EmployeeInvitation.stub(businessId = fixture.businessId, code = "SAMECODE"))
        }

        whenn()
        val result = runCatching {
            suspendTransaction {
                fixture.sut.createInvitation(
                    EmployeeInvitation.stub(businessId = fixture.businessId, code = "SAMECODE")
                )
            }
        }

        then()
        assertTrue(result.exceptionOrNull() is Error.UniqueConstraintFailed)
    }
}

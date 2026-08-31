package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.data.orm.table.ClientTable
import com.bookk.business.domain.api.client.entity.Client
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class ClientDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(BusinessTable, BusinessDashboardTable, BusinessPermissionsTable, BusinessWorkingHoursTable, BusinessDayOffTable, ClientTable)
        val sut = ClientDataSourceImpl()
        val businessSut = BusinessDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            businessId = suspendTransaction {
                businessSut.createBusiness(Uuid.random(), "Test Business", "USD", TimeZone.UTC)
            }.id
        }
    }

    @Test
    fun `should create detached client and retrieve by business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val client = Client.Detached(
            id = Uuid.random(), name = "Alice", lastName = "Smith",
            phone = "+1234567890", email = "alice@test.com"
        )

        whenn()
        suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, client) }
        val clients = suspendTransaction { fixture.sut.getClients(fixture.businessId) }

        then()
        assertEquals(1, clients.size)
        assertEquals("Alice", clients.first().name)
    }

    @Test
    fun `should create integrated client with user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val client = Client.Integrated(
            id = Uuid.random(), name = "Bob", lastName = "Jones",
            phone = "+9876543210", email = "bob@test.com", userId = userId
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }

        then()
        assertTrue(created is Client.Integrated)
        assertEquals(userId, (created as Client.Integrated).userId)
    }

    @Test
    fun `should return empty list when no clients exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val clients = suspendTransaction { fixture.sut.getClients(fixture.businessId) }

        then()
        assertTrue(clients.isEmpty())
    }

    @Test
    fun `should retrieve client by phone`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val phone = "+1112223333"
        val client = Client.Detached(
            id = Uuid.random(), name = "Carol", lastName = "White",
            phone = phone, email = "carol@test.com"
        )
        suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, client) }

        whenn()
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, phone) }

        then()
        assertNotNull(found)
        assertEquals("Carol", found!!.name)
    }

    @Test
    fun `should return null when client not found by phone`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, "+0000000000") }

        then()
        assertNull(found)
    }

    @Test
    fun `should retrieve client by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val client = Client.Detached(
            id = Uuid.random(), name = "Erin", lastName = "Black",
            phone = "+1231231234", email = "erin@test.com"
        )
        val created = suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, client) }

        whenn()
        val found = suspendTransaction { fixture.sut.getClientById(fixture.businessId, created.id) }

        then()
        assertNotNull(found)
        assertEquals("Erin", found!!.name)
    }

    @Test
    fun `should return null when client not found by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val found = suspendTransaction { fixture.sut.getClientById(fixture.businessId, Uuid.random()) }

        then()
        assertNull(found)
    }

    @Test
    fun `should return null when client id belongs to a different business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val otherBusinessId = suspendTransaction {
            fixture.businessSut.createBusiness(Uuid.random(), "Other Business", "USD", TimeZone.UTC)
        }.id
        val client = Client.Detached(
            id = Uuid.random(), name = "Frank", lastName = "Green",
            phone = "+3213214321", email = "frank@test.com"
        )
        val created = suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, client) }

        whenn()
        val found = suspendTransaction { fixture.sut.getClientById(otherBusinessId, created.id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should delete client and return true`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val client = Client.Detached(
            id = Uuid.random(), name = "Dave", lastName = "Brown",
            phone = "+4445556666", email = "dave@test.com"
        )
        val created = suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, client) }

        whenn()
        val deleted = suspendTransaction { fixture.sut.deleteClient(fixture.businessId, created.id) }
        val remaining = suspendTransaction { fixture.sut.getClients(fixture.businessId) }

        then()
        assertTrue(deleted)
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `should return false when deleting non-existent client`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val deleted = suspendTransaction { fixture.sut.deleteClient(fixture.businessId, Uuid.random()) }

        then()
        assertTrue(!deleted)
    }

    @Test
    fun `should update integrated client fields by user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val phone = "+5556667777"
        val client = Client.Integrated(
            id = Uuid.random(), name = "Old", lastName = "Name",
            phone = phone, email = "old@test.com", userId = userId
        )
        suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedClients(userId, "New", "Surname", "new@test.com", null, Instant.fromEpochMilliseconds(1000))
        }
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, phone) }

        then()
        assertEquals(1, updated)
        assertNotNull(found)
        assertTrue(found is Client.Integrated)
        assertEquals("New", found!!.name)
        assertEquals("Surname", found.lastName)
        assertEquals("new@test.com", found.email)
        assertEquals(userId, (found as Client.Integrated).userId)
    }

    @Test
    fun `should update the client phone when the profile supplies one`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val client = Client.Integrated(
            id = Uuid.random(), name = "Old", lastName = "Name",
            phone = "+1111111111", email = "old@test.com", userId = userId
        )
        suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }

        whenn()
        suspendTransaction {
            fixture.sut.updateIntegratedClients(
                userId, "New", "Surname", "new@test.com", "+2222222222", Instant.fromEpochMilliseconds(1000)
            )
        }

        then()
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, "+2222222222") }
        assertEquals("+2222222222", found!!.phone)
    }

    @Test
    fun `should keep the client phone when the profile has none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val phone = "+1111100000"
        val client = Client.Integrated(
            id = Uuid.random(), name = "Old", lastName = "Name",
            phone = phone, email = "old@test.com", userId = userId
        )
        suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }

        whenn()
        suspendTransaction {
            fixture.sut.updateIntegratedClients(
                userId, "New", "Surname", "new@test.com", null, Instant.fromEpochMilliseconds(1000)
            )
        }

        then()
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, phone) }
        assertEquals(phone, found!!.phone)
        assertEquals("New", found.name)
    }

    @Test
    fun `should ignore a profile update older than the one already applied`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val phone = "+5556660001"
        val client = Client.Integrated(
            id = Uuid.random(), name = "Old", lastName = "Name",
            phone = phone, email = "old@test.com", userId = userId
        )
        suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }
        val newer = Instant.fromEpochMilliseconds(2000)
        val older = Instant.fromEpochMilliseconds(1000)
        suspendTransaction { fixture.sut.updateIntegratedClients(userId, "Newer", "Surname", "newer@test.com", null, newer) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedClients(userId, "Older", "Surname", "older@test.com", null, older)
        }
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, phone) }

        then()
        assertEquals(0, updated)
        assertEquals("Newer", found!!.name)
        assertEquals("newer@test.com", found.email)
    }

    @Test
    fun `should apply a profile update newer than the one already applied`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val phone = "+5556660002"
        val client = Client.Integrated(
            id = Uuid.random(), name = "Old", lastName = "Name",
            phone = phone, email = "old@test.com", userId = userId
        )
        suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }
        suspendTransaction {
            fixture.sut.updateIntegratedClients(userId, "First", "Surname", "first@test.com", null, Instant.fromEpochMilliseconds(1000))
        }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedClients(userId, "Second", "Surname", "second@test.com", null, Instant.fromEpochMilliseconds(2000))
        }
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, phone) }

        then()
        assertEquals(1, updated)
        assertEquals("Second", found!!.name)
    }

    @Test
    fun `should not update detached clients when updating by user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val detachedPhone = "+1010101010"
        val detached = Client.Detached(
            id = Uuid.random(), name = "Keep", lastName = "Me",
            phone = detachedPhone, email = "keep@test.com"
        )
        suspendTransaction { fixture.sut.createDetachedClient(fixture.businessId, detached) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedClients(Uuid.random(), "New", "Surname", "new@test.com", null, Instant.fromEpochMilliseconds(1000))
        }
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, detachedPhone) }

        then()
        assertEquals(0, updated)
        assertNotNull(found)
        assertEquals("Keep", found!!.name)
    }

    @Test
    fun `should anonymize integrated client PII and detach the user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val client = Client.Integrated(
            id = Uuid.random(), name = "Alice", lastName = "Smith",
            phone = "+5551234567", email = "alice@test.com", userId = userId
        )
        val created = suspendTransaction { fixture.sut.createIntegratedClient(fixture.businessId, client) }

        whenn()
        val affected = suspendTransaction { fixture.sut.anonymizeClientsByUserId(userId) }
        val found = suspendTransaction { fixture.sut.getClients(fixture.businessId) }.single { it.id == created.id }

        then()
        assertEquals(1, affected)
        assertEquals("Deleted User", found.name)
        assertEquals("", found.lastName)
        assertEquals("", found.phone)
        assertEquals("", found.email)
        assertTrue(found is Client.Detached)
    }

    @Test
    fun `should return zero when anonymizing clients for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val affected = suspendTransaction { fixture.sut.anonymizeClientsByUserId(Uuid.random()) }

        then()
        assertEquals(0, affected)
    }
}

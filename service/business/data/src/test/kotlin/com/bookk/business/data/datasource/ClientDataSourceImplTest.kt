package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
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
import kotlin.uuid.Uuid

internal class ClientDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(BusinessTable, BusinessDashboardTable, BusinessPermissionsTable, ClientTable)
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
            fixture.sut.updateIntegratedClients(userId, "New", "Surname", phone, "new@test.com")
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
            fixture.sut.updateIntegratedClients(Uuid.random(), "New", "Surname", "+9999999999", "new@test.com")
        }
        val found = suspendTransaction { fixture.sut.getClient(fixture.businessId, detachedPhone) }

        then()
        assertEquals(0, updated)
        assertNotNull(found)
        assertEquals("Keep", found!!.name)
    }
}

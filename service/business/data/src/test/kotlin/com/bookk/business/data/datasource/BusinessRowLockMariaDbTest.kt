package com.bookk.business.data.datasource

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.datasource.RowLock
import com.bookk.core.data.database.createDatabase
import com.bookk.core.domain.entity.runSuspendCatching
import com.bookk.core.test.given
import com.bookk.core.test.runIntegrationTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import kotlin.uuid.Uuid

internal class BusinessRowLockMariaDbTest {

    companion object {
        private const val PASSWORD = "test-password"
        private const val PORT = 3306

        private val container = GenericContainer(DockerImageName.parse("mariadb:11.8.3-ubi9"))
            .withEnv("MARIADB_ROOT_PASSWORD", PASSWORD)
            .withExposedPorts(PORT)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2))

        private lateinit var database: Database

        @JvmStatic
        @BeforeAll
        fun startMariaDb() {
            container.start()
            database = createDatabase(
                schemaName = "business",
                dbUrl = "mariadb://${container.host}",
                dbPort = container.getMappedPort(PORT).toString(),
                dbUsername = "root",
                dbPassword = PASSWORD
            )
        }
    }

    private class SutFixture {
        val businessDataSource = BusinessDataSourceImpl()
        val serviceDataSource = ServiceDataSourceImpl()

        suspend fun createBusinessWithGroup(): ServiceGroup = suspendTransaction(db = database) {
            val business = businessDataSource.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC)
            serviceDataSource.createServiceGroup(ServiceGroup.stub(businessId = business.id))
        }

        fun JdbcTransaction.failFastOnLockWait() {
            exec("SET SESSION innodb_lock_wait_timeout = 1")
        }
    }

    @Test
    fun `should see a service committed after the transaction took its snapshot`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()
        val snapshotTaken = CompletableDeferred<Unit>()
        val serviceCommitted = CompletableDeferred<Unit>()

        whenn()
        val hasServices = withContext(Dispatchers.IO) {
            val currencyChange = async {
                suspendTransaction(db = database) {
                    fixture.businessDataSource.getBusinessById(group.businessId)
                    snapshotTaken.complete(Unit)
                    serviceCommitted.await()
                    fixture.businessDataSource.lockBusiness(group.businessId, RowLock.EXCLUSIVE)
                    fixture.serviceDataSource.hasServices(group.businessId)
                }
            }
            snapshotTaken.await()
            suspendTransaction(db = database) {
                fixture.businessDataSource.lockBusiness(group.businessId, RowLock.SHARED)
                fixture.serviceDataSource.createService(Service.stub(businessId = group.businessId, group = group))
            }
            serviceCommitted.complete(Unit)
            currencyChange.await()
        }

        then()
        assertTrue(hasServices)
    }

    @Test
    fun `should let two service writes hold the shared business lock at the same time`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()
        val firstLockHeld = CompletableDeferred<Unit>()
        val secondFinished = CompletableDeferred<Unit>()

        whenn()
        val secondLock = withContext(Dispatchers.IO) {
            val first = async {
                suspendTransaction(db = database) {
                    fixture.businessDataSource.lockBusiness(group.businessId, RowLock.SHARED)
                    firstLockHeld.complete(Unit)
                    secondFinished.await()
                }
            }
            firstLockHeld.await()
            val second = suspendTransaction(db = database) {
                with(fixture) { failFastOnLockWait() }
                runSuspendCatching { fixture.businessDataSource.lockBusiness(group.businessId, RowLock.SHARED) }
            }
            secondFinished.complete(Unit)
            first.await()
            second
        }

        then()
        assertTrue(secondLock.getOrNull() != null)
    }

    @Test
    fun `should make a currency change wait for an in-flight service write`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()
        val sharedLockHeld = CompletableDeferred<Unit>()
        val exclusiveAttempted = CompletableDeferred<Unit>()

        whenn()
        val exclusiveLock = withContext(Dispatchers.IO) {
            val serviceWrite = async {
                suspendTransaction(db = database) {
                    fixture.businessDataSource.lockBusiness(group.businessId, RowLock.SHARED)
                    sharedLockHeld.complete(Unit)
                    exclusiveAttempted.await()
                }
            }
            sharedLockHeld.await()
            val currencyChange = suspendTransaction(db = database) {
                with(fixture) { failFastOnLockWait() }
                runSuspendCatching { fixture.businessDataSource.lockBusiness(group.businessId, RowLock.EXCLUSIVE) }
            }
            exclusiveAttempted.complete(Unit)
            serviceWrite.await()
            currencyChange
        }

        then()
        assertTrue(exclusiveLock.isFailure)
    }

    @Test
    fun `should make a service group deletion wait for a service write using the group`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()
        val groupLockHeld = CompletableDeferred<Unit>()
        val deletionAttempted = CompletableDeferred<Unit>()

        whenn()
        val deletion = withContext(Dispatchers.IO) {
            val serviceWrite = async {
                suspendTransaction(db = database) {
                    fixture.serviceDataSource.lockServiceGroup(group.id, group.businessId)
                    groupLockHeld.complete(Unit)
                    deletionAttempted.await()
                }
            }
            groupLockHeld.await()
            val groupDeletion = suspendTransaction(db = database) {
                with(fixture) { failFastOnLockWait() }
                runSuspendCatching { fixture.serviceDataSource.deleteServiceGroup(group.id, group.businessId) }
            }
            deletionAttempted.complete(Unit)
            serviceWrite.await()
            groupDeletion
        }

        then()
        assertTrue(deletion.isFailure)
    }
}

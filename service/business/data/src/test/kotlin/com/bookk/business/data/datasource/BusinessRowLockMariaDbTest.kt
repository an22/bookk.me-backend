package com.bookk.business.data.datasource

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
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
        val invitationDataSource = EmployeeInvitationDataSourceImpl()

        suspend fun createBusinessWithGroup(): ServiceGroup = suspendTransaction(db = database) {
            val business = businessDataSource.createBusiness(Uuid.random(), "Salon", "USD", TimeZone.UTC)
            serviceDataSource.createServiceGroup(ServiceGroup.stub(businessId = business.id))
        }

        suspend fun createService(group: ServiceGroup): Service = suspendTransaction(db = database) {
            serviceDataSource.createService(Service.stub(businessId = group.businessId, group = group))
        }

        fun recentInvitation(businessId: Uuid) = EmployeeInvitation.stub(
            businessId = businessId,
            code = Uuid.random().toString(),
            createdAt = Clock.System.now()
        )

        suspend fun <T> readAfterWaitingForLockHolder(
            businessId: Uuid,
            holderLock: RowLock,
            holderWrite: suspend SutFixture.() -> Unit,
            read: suspend SutFixture.() -> T
        ): T {
            val holderWrote = CompletableDeferred<Unit>()
            val waiterBlocked = CompletableDeferred<Unit>()
            return withContext(Dispatchers.IO) {
                val holder = async {
                    suspendTransaction(db = database) {
                        businessDataSource.lockBusiness(businessId, holderLock)
                        holderWrite()
                        holderWrote.complete(Unit)
                        waiterBlocked.await()
                    }
                }
                holderWrote.await()
                val waiter = async {
                    suspendTransaction(db = database) {
                        businessDataSource.lockBusiness(businessId, RowLock.EXCLUSIVE)
                        businessDataSource.getBusinessById(businessId)
                        read()
                    }
                }
                awaitTransactionWaitingForLock()
                waiterBlocked.complete(Unit)
                holder.await()
                waiter.await()
            }
        }

        private suspend fun awaitTransactionWaitingForLock() {
            while (transactionsWaitingForLock() == 0L) delay(10.milliseconds)
        }

        private suspend fun transactionsWaitingForLock(): Long = suspendTransaction(db = database) {
            exec("SELECT COUNT(*) FROM information_schema.INNODB_TRX WHERE trx_state = 'LOCK WAIT'") { rows ->
                rows.next()
                rows.getLong(1)
            } ?: 0L
        }

        fun JdbcTransaction.failFastOnLockWait() {
            exec("SET SESSION innodb_lock_wait_timeout = 1")
        }
    }

    @Test
    fun `should see a service committed by the service write the currency change waited for`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()

        whenn()
        val hasServices = fixture.readAfterWaitingForLockHolder(
            businessId = group.businessId,
            holderLock = RowLock.SHARED,
            holderWrite = { serviceDataSource.createService(Service.stub(businessId = group.businessId, group = group)) },
            read = { serviceDataSource.hasServices(group.businessId) }
        )

        then()
        assertTrue(hasServices)
    }

    @Test
    fun `should read an edited service without error after waiting for the edit`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val group = fixture.createBusinessWithGroup()
        val service = fixture.createService(group)

        whenn()
        val services = fixture.readAfterWaitingForLockHolder(
            businessId = group.businessId,
            holderLock = RowLock.SHARED,
            holderWrite = { serviceDataSource.editService(service.copy(name = "Renamed")) },
            read = { serviceDataSource.getServices(group.businessId) }
        )

        then()
        assertEquals(listOf("Renamed"), services.map { it.name })
    }

    @Test
    fun `should count a pending invitation committed by the creation it waited for`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val businessId = fixture.createBusinessWithGroup().businessId

        whenn()
        val pendingInvitations = fixture.readAfterWaitingForLockHolder(
            businessId = businessId,
            holderLock = RowLock.EXCLUSIVE,
            holderWrite = { invitationDataSource.createInvitation(recentInvitation(businessId)) },
            read = { invitationDataSource.countPendingInvitations(businessId) }
        )

        then()
        assertEquals(1, pendingInvitations)
    }

    @Test
    fun `should count a recent invitation committed by the creation it waited for`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val businessId = fixture.createBusinessWithGroup().businessId

        whenn()
        val invitationsToday = fixture.readAfterWaitingForLockHolder(
            businessId = businessId,
            holderLock = RowLock.EXCLUSIVE,
            holderWrite = { invitationDataSource.createInvitation(recentInvitation(businessId)) },
            read = { invitationDataSource.countInvitationsCreatedSince(businessId, Instant.fromEpochMilliseconds(0)) }
        )

        then()
        assertEquals(1, invitationsToday)
    }

    @Test
    fun `should miss rows committed while waiting when the snapshot was taken before the lock`() = runIntegrationTest {
        given()
        val fixture = SutFixture()
        val businessId = fixture.createBusinessWithGroup().businessId
        val snapshotTaken = CompletableDeferred<Unit>()
        val invitationCommitted = CompletableDeferred<Unit>()

        whenn()
        val pendingInvitations = withContext(Dispatchers.IO) {
            val lateLocker = async {
                suspendTransaction(db = database) {
                    fixture.businessDataSource.getBusinessById(businessId)
                    snapshotTaken.complete(Unit)
                    invitationCommitted.await()
                    fixture.businessDataSource.lockBusiness(businessId, RowLock.EXCLUSIVE)
                    fixture.invitationDataSource.countPendingInvitations(businessId)
                }
            }
            snapshotTaken.await()
            suspendTransaction(db = database) {
                fixture.businessDataSource.lockBusiness(businessId, RowLock.EXCLUSIVE)
                fixture.invitationDataSource.createInvitation(fixture.recentInvitation(businessId))
            }
            invitationCommitted.complete(Unit)
            lateLocker.await()
        }

        then()
        assertEquals(0, pendingInvitations)
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
    fun `should make an exclusive lock wait for an in-flight service write`() = runIntegrationTest {
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
            val exclusive = suspendTransaction(db = database) {
                with(fixture) { failFastOnLockWait() }
                runSuspendCatching { fixture.businessDataSource.lockBusiness(group.businessId, RowLock.EXCLUSIVE) }
            }
            exclusiveAttempted.complete(Unit)
            serviceWrite.await()
            exclusive
        }

        then()
        assertFalse(exclusiveLock.isSuccess)
    }
}

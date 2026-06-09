package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentRequestStatus
import com.bookk.appointments.domain.api.operation.DeclineAppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class DeclineAppointmentRequestImplTest {

    private class SutFixture {
        val requestDataSource = mockk<AppointmentRequestDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val transactionManager = mockk<TransactionManager>()

        val sut = DeclineAppointmentRequestImpl(
            requestDataSource,
            permissionsDataSource,
            subscriptionDataSource,
            eventProducer,
            transactionManager
        )
    }

    private val testUserId = Uuid.random()
    private val testBusinessId = Uuid.random()
    private val testCancellation = AppointmentCancellation(
        id = Uuid.random(),
        businessId = testBusinessId,
        reason = "User declined"
    )

    @Test
    fun `should decline request successfully`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val request = AppointmentRequest.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentRequestStatus.PENDING)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.requestDataSource.get(testCancellation.id) } returns request
        coEvery { fixture.requestDataSource.decline(testCancellation.id, testCancellation.reason) } returns request.copy(status = AppointmentRequestStatus.DECLINED)
        coEvery { fixture.subscriptionDataSource.getBusinessSnapshot(testBusinessId) } returns mockk(relaxed = true)

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when already declined`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val request = AppointmentRequest.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentRequestStatus.DECLINED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.requestDataSource.get(testCancellation.id) } returns request

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DeclineAppointmentRequest.Error.AlreadyDeclined)
    }

    @Test
    fun `should return failure when already approved`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val request = AppointmentRequest.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentRequestStatus.APPROVED)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.requestDataSource.get(testCancellation.id) } returns request

        whenn()
        val result = fixture.sut.invoke(testUserId, testCancellation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DeclineAppointmentRequest.Error.AlreadyApproved)
    }

    @Test
    fun `should send request declined event successfully`() = runUnitTest {
        val fixture = SutFixture()
        given()
        fixture.transactionManager.mockTransaction()
        val request = AppointmentRequest.stub(id = testCancellation.id, businessId = testBusinessId)
            .copy(status = AppointmentRequestStatus.PENDING)

        coEvery { fixture.permissionsDataSource.getPermissions(testUserId, testBusinessId) } returns ObjectPermission.EDIT.int
        coEvery { fixture.requestDataSource.get(testCancellation.id) } returns request
        coEvery { fixture.requestDataSource.decline(testCancellation.id, testCancellation.reason) } returns request.copy(status = AppointmentRequestStatus.DECLINED)
        coEvery { fixture.subscriptionDataSource.getBusinessSnapshot(testBusinessId) } returns mockk(relaxed = true)

        whenn()
        fixture.sut.invoke(testUserId, testCancellation)

        then()
        coVerify(exactly = 1) { fixture.eventProducer.send(any(AppointmentEvent.RequestRejected::class), any()) }
    }
}

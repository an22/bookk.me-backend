package com.bookk.appointments.domain.impl.operation

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.bookk.appointments.domain.api.entity.AppointmentOffer
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import com.bookk.server.business.client.api.QuoteClaims
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.datetime.TimeZone
import library.signing.TokenValidator
import library.signing.TokenValidatorFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateAppointmentRequestImplTest {

    private val futureDate = Instant.parse("2099-01-01T00:00:00Z")

    private class SutFixture {
        val requestDataSource = mockk<AppointmentRequestDataSource>()
        val appointmentDataSource = mockk<AppointmentDataSource>()
        val settingsDataSource = mockk<AppointmentSettingsDataSource>()
        val createAppointment = mockk<CreateAppointment>()
        val transactionManager = mockk<TransactionManager>()
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val eventProducer = mockk<StandardEventProducer>()
        val tokenValidatorFactory = mockk<TokenValidatorFactory>()

        val sut = CreateAppointmentRequestImpl(
            requestDataSource,
            appointmentDataSource,
            settingsDataSource,
            subscriptionDataSource,
            eventProducer,
            createAppointment,
            transactionManager,
            tokenValidatorFactory
        )

        fun mockValidToken(request: AppointmentRequest) {
            val jwtVerifier = mockk<JWTVerifier>()
            val decodedJwt = mockk<DecodedJWT>()
            val servicesClaim = mockk<Claim>()
            val totalClaim = mockk<Claim>()
            val businessIdClaim = mockk<Claim>()
            val validator = mockk<TokenValidator>()

            coEvery { requestDataSource.isTokenInCache("token") } returns false
            coEvery { requestDataSource.cacheOfferToken("token") } returns Unit
            every { validator.verifier } returns jwtVerifier
            every { tokenValidatorFactory.forType(any()) } returns validator
            every { jwtVerifier.verify("token") } returns decodedJwt
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_SERVICES) } returns servicesClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_TOTAL) } returns totalClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_BUSINESS_ID) } returns businessIdClaim
            every { servicesClaim.asList(String::class.java) } returns request.services.map { it.id.toString() }
            every { totalClaim.asString() } returns request.totalAmount.toString()
            every { businessIdClaim.asString() } returns request.businessId.toString()
        }
    }

    @Test
    fun `should create request successfully when valid request provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(any()) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.requestDataSource.create(request) }
    }

    @Test
    fun `should create request successfully when valid request provided with automatic approval`() = runUnitTest {
        given()

        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            coEvery { createAppointment.invoke(userId, request) } returns Result.success(mockk())
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { fixture.requestDataSource.create(request) }
        coVerify(exactly = 1) { fixture.createAppointment.invoke(userId, request) }
    }

    @Test
    fun `should return failure when date is not in workday`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns false
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisDateNotAllowed)
    }

    @Test
    fun `should return failure when request date is in the past`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val pastDate = Instant.parse("2000-01-01T00:00:00Z")
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = pastDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.DateInThePastNotAllowed)
    }

    @Test
    fun `should return failure when create request fails`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            coEvery { createAppointment.invoke(userId, request) } returns Result.failure(RuntimeException())
            mockValidToken(request)
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `should return failure when time is not in worktime`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns false
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed)
    }

    @Test
    fun `should create request successfully when caller has no business permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(any()) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when create event fails to be sent`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } answers { throw RuntimeException("Producer fail") }
            mockValidToken(request)
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Producer fail", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return failure when request overlaps with existing appointment`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns true
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.RequestForThisTimeExists)
    }

    @Test
    fun `should send event if appointment created`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(businessId) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            mockValidToken(request)
            transactionManager.mockTransaction()
        }
        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.eventProducer.send(any(AppointmentEvent.RequestCreated::class), any()) }
    }

    @Test
    fun `should return failure when price changed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { requestDataSource.isTokenInCache("token") } returns false
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            val jwtVerifier = mockk<JWTVerifier>()
            val decodedJwt = mockk<DecodedJWT>()
            val servicesClaim = mockk<Claim>()
            val totalClaim = mockk<Claim>()
            val businessIdClaim = mockk<Claim>()
            val validator = mockk<TokenValidator>()

            every { validator.verifier } returns jwtVerifier
            every { tokenValidatorFactory.forType(any()) } returns validator
            every { jwtVerifier.verify("token") } returns decodedJwt
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_SERVICES) } returns servicesClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_TOTAL) } returns totalClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_BUSINESS_ID) } returns businessIdClaim
            every { servicesClaim.asList(String::class.java) } returns request.services.map { it.id.toString() }
            every { totalClaim.asString() } returns "USD 0.00"
            every { businessIdClaim.asString() } returns businessId.toString()
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.PriceChanged)
    }

    @Test
    fun `should return failure when services signature does not match`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { requestDataSource.isTokenInCache("token") } returns false
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            val jwtVerifier = mockk<JWTVerifier>()
            val decodedJwt = mockk<DecodedJWT>()
            val servicesClaim = mockk<Claim>()
            val totalClaim = mockk<Claim>()
            val businessIdClaim = mockk<Claim>()
            val validator = mockk<TokenValidator>()

            every { validator.verifier } returns jwtVerifier
            every { tokenValidatorFactory.forType(any()) } returns validator
            every { jwtVerifier.verify("token") } returns decodedJwt
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_SERVICES) } returns servicesClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_TOTAL) } returns totalClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_BUSINESS_ID) } returns businessIdClaim
            every { servicesClaim.asList(String::class.java) } returns listOf(Uuid.random().toString())
            every { totalClaim.asString() } returns request.totalAmount.toString()
            every { businessIdClaim.asString() } returns businessId.toString()
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.ServicesSignatureMiss)
    }

    @Test
    fun `should return failure when business id in token does not match request`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()

        with(fixture) {
            coEvery { requestDataSource.isTokenInCache("token") } returns false
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            val jwtVerifier = mockk<JWTVerifier>()
            val decodedJwt = mockk<DecodedJWT>()
            val servicesClaim = mockk<Claim>()
            val totalClaim = mockk<Claim>()
            val businessIdClaim = mockk<Claim>()
            val validator = mockk<TokenValidator>()

            every { validator.verifier } returns jwtVerifier
            every { tokenValidatorFactory.forType(any()) } returns validator
            every { jwtVerifier.verify("token") } returns decodedJwt
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_SERVICES) } returns servicesClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_TOTAL) } returns totalClaim
            every { decodedJwt.getClaim(QuoteClaims.CLAIM_BUSINESS_ID) } returns businessIdClaim
            every { servicesClaim.asList(String::class.java) } returns request.services.map { it.id.toString() }
            every { totalClaim.asString() } returns request.totalAmount.toString()
            every { businessIdClaim.asString() } returns Uuid.random().toString()
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.ServicesSignatureMiss)
    }

    @Test
    fun `should return TokenAlreadyUsed when offer token is already in cache`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, date = futureDate)
        val offer = AppointmentOffer(request, "token")

        with(fixture) {
            coEvery { requestDataSource.isTokenInCache("token") } returns true
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateAppointmentRequest.Error.TokenAlreadyUsed)
        coVerify(exactly = 0) { fixture.requestDataSource.cacheOfferToken(any()) }
        coVerify(exactly = 0) { fixture.transactionManager.transaction<Any>(any()) }
    }

    @Test
    fun `should cache token after successful claim validation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(any()) } returns businessSnapshot
            coEvery { eventProducer.send(any(), any()) } returns Unit
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.requestDataSource.cacheOfferToken("token") }
    }

    @Test
    fun `should propagate business time zone onto the published event`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId, businessId = businessId, date = futureDate)
        val offer = AppointmentOffer(request, "token")
        val settings = mockk<AppointmentSettings>()
        val businessSnapshot = BusinessSnapshot.stub().copy(timeZone = TimeZone.of("Europe/Kyiv"))
        val eventSlot = slot<AppointmentEvent.RequestCreated>()

        with(fixture) {
            coEvery { settingsDataSource.getForUpdate(businessId) } returns settings
            coEvery { settings.automaticApproval } returns false
            coEvery { settings.isInWorkday(request.date) } returns true
            coEvery { settings.isInWorktime(request.date, request.dateEnd) } returns true
            coEvery { requestDataSource.hasOverlapsWith(request) } returns false
            coEvery { appointmentDataSource.hasOverlapsWith(request) } returns false
            coEvery { requestDataSource.create(request) } returns request
            coEvery { subscriptionDataSource.getBusinessSnapshot(any()) } returns businessSnapshot
            coEvery { eventProducer.send(capture(eventSlot), any()) } returns Unit
            mockValidToken(request)
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke(userId, offer)

        then()
        assertTrue(result.isSuccess)
        assertEquals(TimeZone.of("Europe/Kyiv"), eventSlot.captured.timeZone)
    }
}

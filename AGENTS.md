# Project Architecture & Testing Guidelines

## Architecture Overview
The project is structured as a modular monolith split into microservices, libraries, and core modules:
- `core/`: Shared logic, authentication, and testing infrastructure.
- `library/`: Independent utilities (idempotency, money, permissions).
- `service/`: Domain-specific business logic and microservice web layers:
  - `domain/api`: Public interfaces, entities, operations, and error definitions.
  - `domain/impl`: Implementation of domain operations.
  - `data/`: Persistence layers (Exposed ORM).
  - `microservice/`: Ktor-based routing, authentication, and dependency injection (Koin).

## Unit Test Generation Guidelines

### 1. Web Layer (Ktor Routing) Tests
All routes in `microservice/` must be tested using `routeTest`.

- **Location**: Place tests in `service/<service>/microservice/src/test/kotlin/...`, if directory not exists, create it.
- **Infrastructure**: Use provided `testFixtures` in `core/service/`:
  - `setupApplication`: To mock Koin DI modules and Ktor plugins (Auth/Negotiation).
  - `routeTest` / `createTestClient`: To run isolated tests.
- **Requirements**:
  - Mock external `domain.api` operations using `mockk`.
  - Provide `given()`, `whenn()`, `then()` calls within `routeTest` blocks for consistency with existing codebase.
  - Authenticate using custom providers when routes are wrapped in `authenticate { ... }`.
  - Use `Result` types as returned by operations; assert against status codes (e.g., `HttpStatusCode.OK` for success, `NoContent` for deletions).
  - Use typed resource classes (e.g., `UserRouting.Api.User.Me()`) for request building.
  - DO NOT EDIT bookk-server/core/src/testFixtures/kotlin/com/bookk/core/test/Test.kt
  - To create unauthorized test, in AuthenticationPlugin use `bearer { authenticate { null } }`
  - SUT must be created for every test instance separately, no instance sharing
  - Do not share mocks between unit tests

### 2. Implementation & Data Tests
- **Operations**: Test domain operation implementations using pure JUnit/MockK in `domain/impl`.
- **Data Sources**: Test persistence logic using an in-memory database or mocks where applicable.

### 3. Dependencies & Best Practices
- **Dependencies**: Add `testImplementation(testFixtures(projects.core.service))` and any specific libs (e.g., `libs.joda.money`) to `build.gradle.kts`.
- **Serialization**: Ensure data entities used in requests/responses are handled by the registered `SerializationConverter` (ProtoBuf). Provide real entity instances for mocks to avoid serialization exceptions (NPEs).
- **Coroutines**: NEVER use `runBlocking` in tests. ALWAYS use `runUnitTest` from provided core fixtures to handle suspended functions.
- **Naming Convention**: Use behavioral sentence style for test function names enclosed in backticks (e.g., `` `should return business when found` ``).
- **Versioning**: Ensure transitive dependencies (e.g., Jackson) are managed via `resolutionStrategy` in the root `build.gradle.kts` if conflicts arise.

### 4. Unit testing guide

This document establishes guidelines for AI agents (and human developers) writing, reviewing,
and maintaining unit tests in Kotlin projects, using **MockK** as the mocking library and
**Ktor** for server-side testing.

---

## 1. Core Principles

### Test one thing at a time
Each test verifies a single behavior or code path. If a test fails, its name alone should
identify exactly what broke.

### Tests are documentation
Well-named tests describe the contract of your code. Write them as if they are the spec.

### Fast, isolated, deterministic
Unit tests must not hit real databases, filesystems, network sockets, or clocks.
Inject or mock every external dependency.

### Given – When – Then
Structure every test in three clearly separated phases:
1. **Given** — set up inputs, mocks, and preconditions
2. **When** — call the unit under test
3. **Then** — verify the outcome

---

### 1. Core Principles (Continued)

#### Test Isolation Mandate
- **No Shared State**: SUT (System Under Test) and ALL mocks MUST be initialized *within* each individual test method.
- **No Class-Level Sharing**: DO NOT initialize SUT or mocks as class properties. Shared state between tests leads to flaky, order-dependent behavior.

---

## 2. Naming Conventions

Use backtick test names for readable, sentence-style descriptions:

```kotlin
@Test
fun `calculateTax with zero income returns zero`() { ... }

@Test
fun `parseDate with invalid string throws ParseException`() { ... }

@Test
fun `sendEmail when SMTP is unavailable retries three times`() { ... }
```

Avoid vague names like `test1`, `testHappy`, or `works`.

---

## 3. Test Structure (AAA)

```kotlin
@Test
fun `calculateDiscount for VIP customer returns 20 percent`() = runUnitTest {
    given()
    val customer = Customer(tier = Tier.VIP)
    val order = Order(subtotal = 100.0)

    whenn()
    val discount = calculateDiscount(customer, order)

    then()
    assertEquals(20.0, discount)
}
```

---

## 4. MockK — Core Usage

### Creating mocks

```kotlin
// Standard mock — all calls must be stubbed or will throw
val userRepository = mockk<UserRepository>()

// Relaxed mock — unstubbed calls return default values
val userRepository = mockk<UserRepository>(relaxed = true)

// Relaxed Unit mock — only Unit-returning functions are relaxed
val eventBus = mockk<EventBus>(relaxUnitFun = true)

// Spy — wraps a real object, stubs only what you specify
val service = spyk(UserService(userRepository))
```

### Stubbing with `every`

```kotlin
every { userRepository.findById(42) } returns User(id = 42, name = "Alice")

// Stub with argument matchers
every { userRepository.findById(any()) } returns null

// Stub to throw
every { userRepository.findById(-1) } throws IllegalArgumentException("Invalid ID")

// Stub sequential calls
every { counter.next() } returnsMany listOf(1, 2, 3)

// Stub with answer (access call arguments)
every { userRepository.save(any()) } answers { firstArg() }
```

### Verification with `verify`

```kotlin
// Verify called exactly once (default)
verify { userRepository.save(any()) }

// Verify call count
verify(exactly = 2) { emailService.send(any()) }

// Verify never called
verify(exactly = 0) { auditLog.record(any()) }

// Verify order of calls
verifyOrder {
    userRepository.findById(42)
    emailService.send(any())
}

// Verify all verifications are accounted for (no unexpected calls)
confirmVerified(userRepository, emailService)
```

### Capturing arguments

```kotlin
val slot = slot<User>()

every { userRepository.save(capture(slot)) } just Runs

service.register("alice@example.com")

assertEquals("alice@example.com", slot.captured.email)
```

### Mocking object methods, companion objects, and top-level functions

```kotlin
// Top-level / static function
mockkStatic(::generateId)
every { generateId() } returns "fixed-id-123"

// Object
mockkObject(FeatureFlags)
every { FeatureFlags.isEnabled("new-ui") } returns true

// Companion object
mockkObject(User.Companion)
every { User.Companion.default() } returns User(id = 0, name = "Guest")

// Always unmock after use
unmockkStatic(::generateId)
unmockkObject(FeatureFlags)
```

### Coroutine support

```kotlin
// Stub suspend functions
coEvery { userRepository.findById(42) } returns User(id = 42, name = "Alice")

// Verify suspend calls
coVerify { userRepository.findById(42) }

// In test body — use runTest (kotlinx-coroutines-test)
@Test
fun `findUser returns user when found`() = runUnitTest {
    coEvery { userRepository.findById(42) } returns User(id = 42, name = "Alice")

    val result = userService.findUser(42)

    assertEquals("Alice", result?.name)
}
```

---

## 6. Assertions

Prefer JUnit assertions

---

## 7. What to Test

| Category | Test it |
|---|---|
| Happy path | Core logic works with valid inputs |
| Boundary values | Min, max, empty, zero, null |
| Error paths | Exceptions thrown, error results returned |
| State changes | Side effects and mutations |
| Contracts | Public API surface and return types |
| Coroutine cancellation | Proper cleanup on `CancellationException` |

### Do NOT test
- Private implementation details
- Third-party library internals (MockK, Ktor, Kotlin stdlib)
- Generated code or data class `copy` / `equals` boilerplate
- Code you don't own

---

## 8. Test Data and Factories

Avoid hardcoded literals scattered through tests. Use factory functions with defaults:

```kotlin
fun aUser(
    id: Int = 1,
    email: String = "user@example.com",
    role: Role = Role.VIEWER,
    active: Boolean = true,
) = User(id = id, email = email, role = role, active = active)

fun anOrder(
    id: Int = 100,
    subtotal: Double = 50.0,
    customerId: Int = 1,
) = Order(id = id, subtotal = subtotal, customerId = customerId)

// Usage — only specify what matters for the test
val admin = aUser(role = Role.ADMIN)
val inactiveUser = aUser(active = false)
```

---

## 9. Parameterized Tests

```kotlin
// JUnit 5 parameterized
@ParameterizedTest
@MethodSource("discountCases")
fun `calculateDiscount returns expected value`(tier: Tier, expected: Double) {
    val result = calculateDiscount(aUser(), anOrder(subtotal = 100.0))
    result shouldBe expected
}

companion object {
    @JvmStatic
    fun discountCases() = Stream.of(
        Arguments.of(Tier.STANDARD,  0.0),
        Arguments.of(Tier.SILVER,   10.0),
        Arguments.of(Tier.GOLD,     15.0),
        Arguments.of(Tier.VIP,      20.0),
    )
}
```
---

## 10. Ktor Unit Testing

### Basic test structure

```kotlin
class PostValidateRegistrationTest {

  private fun createSimpleRequest(): VerifyAccountCreationRequest {
    return VerifyAccountCreationRequest(
      requestId = "user_id",
      deviceInfo = VerifyAccountCreationRequest.DeviceInfo(
        deviceUUID = Uuid.random(),
        deviceName = "example_device_name"
      ),
      userInfo = VerifyAccountCreationRequest.UserInfo(
        name = "name",
        lastName = "lastName",
        email = "email"
      ),
      publicKeyCredentialJson = "mock"
    )
  }

  @Test
  fun invalidEmailFormat() = routeTest {
    given()
    val useCase: FinishRegistration = mockk()
    val client = createTestClient()
    val request = createSimpleRequest()
    coEvery { useCase.invoke(any()) } returns Result.failure(InvalidEmailFormat)
    setupApplication(
      diModule = module {
        single<FinishRegistration> { useCase }
      },
      routeUnderTest = { registration() }
    )
    whenn()
    val response = client.post(AuthRouting.Api.Auth.SignUp()) {
      setBody(request)
    }
    val body = response.body<SimpleServerError>()
    then()
    coVerify { useCase.invoke(eq(request)) }
    assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    assertEquals(AuthErrorCodes.INVALID_EMAIL_FORMAT, body.errorCode)
    assertEquals(InvalidEmailFormat.message, body.message)
  }

  @Test
  fun userAlreadyExist() = routeTest {
    given()
    val useCase: FinishRegistration = mockk()
    val client = createTestClient()
    val request = createSimpleRequest()
    coEvery { useCase.invoke(any()) } returns Result.failure(UserAlreadyExist)
    setupApplication(
      diModule = module {
        single<FinishRegistration> { useCase }
      },
      routeUnderTest = { registration() }
    )
    whenn()
    val response = client.post(AuthRouting.Api.Auth.SignUp()) {
      setBody(request)
    }
    val body = response.body<SimpleServerError>()
    then()
    coVerify { useCase.invoke(eq(request)) }
    assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    assertEquals(AuthErrorCodes.USER_ALREADY_EXIST, body.errorCode)
    assertEquals(UserAlreadyExist.message, body.message)
  }

  @Test
  fun verificationFailed() = routeTest {
    given()
    val useCase: FinishRegistration = mockk()
    val client = createTestClient()
    val request = createSimpleRequest()
    coEvery { useCase.invoke(any()) } returns Result.failure(VerificationFailed)
    setupApplication(
      diModule = module {
        single<FinishRegistration> { useCase }
      },
      routeUnderTest = { registration() }
    )
    whenn()
    val response = client.post(AuthRouting.Api.Auth.SignUp()) {
      setBody(request)
    }
    val body = response.body<SimpleServerError>()
    then()
    coVerify { useCase.invoke(eq(request)) }
    assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    assertEquals(AuthErrorCodes.VERIFICATION_FAILED, body.errorCode)
    assertEquals(VerificationFailed.message, body.message)
  }

  @Test
  fun accountCreationFailed() = routeTest {
    given()
    val useCase: FinishRegistration = mockk()
    val client = createTestClient()
    val request = createSimpleRequest()
    coEvery { useCase.invoke(any()) } returns Result.failure(AccountCreationFailed)
    setupApplication(
      diModule = module {
        single<FinishRegistration> { useCase }
      },
      routeUnderTest = { registration() }
    )
    whenn()
    val response = client.post(AuthRouting.Api.Auth.SignUp()) {
      setBody(request)
    }
    val body = response.body<SimpleServerError>()
    then()
    coVerify { useCase.invoke(eq(request)) }
    assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    assertEquals(AuthErrorCodes.ACCOUNT_CREATION_FAILED, body.errorCode)
    assertEquals(AccountCreationFailed.message, body.message)
  }

  @Test
  fun successResponse() = routeTest {
    given()
    val useCase: FinishRegistration = mockk()
    val client = createTestClient()
    val request = createSimpleRequest()
    val expected = AuthTokens("access_token", "refresh_token")
    coEvery { useCase.invoke(any()) } returns Result.success(expected)
    setupApplication(
      diModule = module {
        single<FinishRegistration> { useCase }
      },
      routeUnderTest = { registration() }
    )
    whenn()
    val response = client.post(AuthRouting.Api.Auth.SignUp()) {
      setBody(request)
    }
    val body = response.body<AuthTokens>()
    then()
    coVerify { useCase.invoke(eq(request)) }
    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals(expected, body)
  }
}
```



## Mandatory Testing Strategy (Contract-First)

For every new module, feature, or route, you MUST enumerate the following test scenarios BEFORE writing code:
1.  **Success Scenarios**: The "Happy Path".
2.  **Domain Error Scenarios (Coverage Checklist)**:
    -   You MUST locate the `sealed interface Error` definition.
    -   Create a **checklist** in your plan, mapping EVERY case to a test.
        - `[ ] ErrorTypeA: Validates Status Code X and Error Code Y`
        - `[ ] ErrorTypeB: Validates Status Code Z and Error Code W`
    -   Do not proceed to execution until this checklist is created.
3.  **Authentication/Authorization Scenarios**: 
    -   Unauthenticated (401 Unauthorized).
    -   Insufficient permissions (403 Forbidden).
4.  **Technical Failure Scenarios**: Unexpected exceptions (500 Internal Server Error).

### Final Verification Step
BEFORE marking a task as complete, you MUST:
1.  Review your implemented test suite against the original **Domain Error Scenarios (Coverage Checklist)**.
2.  Explicitly confirm in the final response that **ALL** error cases have corresponding test implementations.
3.  Report any missed coverage explicitly.

Update the module's documentation or test plan file (or use an `enter_plan_mode` block) to confirm these scenarios are identified.

---

## 11. Ktor testing checklist

- [ ] Test each route for all expected status codes (200, 201, 400, 401, 403, 404, 422, 500)
- [ ] Test request validation — missing fields, wrong types, out-of-range values
- [ ] Test authentication / authorization — specifically verify 401 Unauthorized for unauthenticated requests and 403 Forbidden for insufficient permissions
- [ ] Mock the `Service` layer with MockK; do not mock Ktor internals
- [ ] Assert both `response.status` AND the response body shape
- [ ] Validate response error code against the operation's `Error` interface (for all domain errors)
- [ ] Use `routeTest`


---

## 11. Coverage Guidelines

| Metric | Target |
|---|---|
| Line coverage | ≥ 80% |
| Branch coverage | ≥ 75% |
| Critical paths (auth, payments, data integrity) | 100% |
| Ktor routes | Every route has at least one success + one error test |

Coverage is a **floor, not a goal**. 100% coverage with weak assertions is worthless.

---

## 12. Performance

- Unit tests must finish in **< 100 ms each**
- `routeTest` tests are fast (in-process); still keep them under **500 ms each**
- If a test is slow it is probably doing real I/O — move it to the integration suite

---

## 13. Anti-Patterns to Avoid

| Anti-pattern | Why it's harmful |
|---|---|
| Using `mockk<T>(relaxed = true)` everywhere | Masks missing stubs; prefer explicit stubs |
| `verify` without `confirmVerified` | Unexpected extra calls go undetected |
| Sharing mutable mock state between tests | Order-dependent, flaky tests |
| `Thread.sleep()` or `delay()` in tests | Flaky and slow; use `TestCoroutineScheduler` |
| Testing private methods directly | Tests break on refactor, not on regression |
| Catching exceptions to silence them | Hides real failures |
| Commenting out failing tests | Creates false confidence in CI |
| Spinning up a real Ktor server for unit tests | Slow; use `testApplication` instead |

---

## 14. Directory Structure

```
src/
  main/kotlin/com/example/
    domain/
      UserService.kt
    routes/
      UserRoutes.kt
    repository/
      UserRepository.kt
test/kotlin/com/example/
  domain/
    UserServiceTest.kt            ← mirrors main structure
  routes/
    UserRoutesTest.kt
  repository/
    UserRepositoryTest.kt
  fixtures/
    Factories.kt                  ← shared factory functions (aUser, anOrder, …)
  integration/
    UserRegistrationFlowTest.kt
```

---

## 15. Code Review Checklist for Tests

Before merging, verify:

- [ ] Test name clearly describes the scenario and expected outcome
- [ ] Test follows AAA structure
- [ ] No real I/O — database, network, filesystem
- [ ] No shared mutable state between tests
- [ ] MockK stubs are explicit (`every` / `coEvery`)
- [ ] `verify` / `coVerify` used where side-effect calls matter
- [ ] `unmockkAll()` called in `@AfterEach` when static/object mocks are used
- [ ] Ktor routes tested for both success and error status codes
- [ ] New behavior added → corresponding test added
- [ ] No skipped / commented-out tests without a linked issue

---

## References

- [MockK documentation](https://mockk.io/)
- [Ktor testing documentation](https://ktor.io/docs/server-testing.html)
- [kotlinx-coroutines-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
- [JUnit 5 parameterized tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests)
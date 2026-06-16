# bookk-server — Agent Playbook

Modular monolith split into Ktor microservices. Kotlin + Ktor (CIO) + Koin DI + Exposed ORM + kotlinx ProtoBuf serialization + MockK/JUnit5 tests. Follow the recipes below verbatim; they mirror the real code (reference implementation: `service/appointments`, the newest service).

## Module map

```
core/                    shared infra: domain Result/Error types, service (Ktor server, auth, respondWith), data (Exposed, event streaming, cache)
library/                 money, idempotency, permissions
service/<svc>/
  domain/api/            operation interfaces + entities + <Svc>ErrorCodes   (package com.bookk.<svc>.domain.api.{operation,entity})
  domain/impl/           <Operation>Impl + di/DI.kt + tests                  (package com.bookk.<svc>.domain.impl.{operation,di})
  data/source/           datasource INTERFACES                               (package com.bookk.<svc>.domain.datasource)
  data/                  datasource impls, orm tables/entities, migration, di
  microservice/          route/<Svc>Routing.kt (typed Resources), route/api/*Route.kt, <Svc>Microservice.kt main, route tests
  client/                cross-service events/API
```

New gradle modules must be registered in `settings.gradle.kts` (one `include` per submodule, grouped per service). Convention plugins (`libs.plugins.bookk.microservice`, `bookk.domain.impl`, `bookk.domain.api`, `bookk.data`, …) already add Ktor/Koin/MockK/kotlin-test deps and JUnit platform — do not re-add them.

## Core conventions (apply everywhere)

- Operations return `Result<T>`; impls **throw** errors inside `transactionManager.transaction { }`, which catches and converts to `Result.failure`.
- Domain errors: nested `sealed interface Error` in the operation interface; each case is a `class` extending `BusinessError(statusCode, code, message)` (classes, NOT data objects). Assert with `is`, never equality.
- Error codes live in `domain/api/.../<Svc>ErrorCodes` as `BASE + n`. Blocks: auth=0, user=100000, business=200000, appointments=300000. Next service takes the next 100000 block.
- Generic infrastructure errors: `com.bookk.core.domain.entity.Error` (`NotFound`, `OperationNotAllowed`, …).
- `call.respondWith(result)` (core/service) maps: success Unit→204, success T→200, `BusinessError`→its statusCode + `SimpleServerError(errorCode, message)`, `Error.NotFound`/`Error.OperationNotAllowed`→**404** (intentional: permission failures do NOT return 403), anything else→500 (logged).
- Permissions: `permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.EDIT)` (library/permissions) — throws `Error.OperationNotAllowed`.
- Wire format is ProtoBuf (`application/x-protobuf`) for all bodies/responses.
- Entities: `@Serializable data class` in `domain/api/.../entity` with a `companion object { fun stub(...) }` factory (defaulted params, `Uuid.random()`, `Instant.fromEpochMilliseconds(0)`) — add `stub()` to every new entity; tests rely on it.

## Recipe: new business operation

Files to touch (example names from appointments):

1. `domain/api/.../operation/DoThing.kt` — interface + errors:
```kotlin
interface DoThing {
    suspend operator fun invoke(userId: Uuid, ...): Result<Thing>

    sealed interface Error {
        class ThingExists : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = SvcErrorCodes.THING_EXISTS,
            message = "Thing already exists"
        ), Error
    }
}
```
2. Add the code constant to `<Svc>ErrorCodes`.
3. `domain/impl/.../operation/DoThingImpl.kt`:
```kotlin
internal class DoThingImpl(
    private val thingDataSource: ThingDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer, // only if events sent
) : DoThing {
    override suspend fun invoke(userId: Uuid, ...): Result<Thing> = transactionManager.transaction {
        val thing = thingDataSource.get(id) ?: throw Error.NotFound()
        permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.EDIT)
        if (conflict) throw DoThing.Error.ThingExists()
        thingDataSource.create(...) // also { eventProducer.send(SvcEvent.X(...)) } if needed
    }
}
```
4. Register in `domain/impl/.../di/DI.kt`: `factoryOf(::DoThingImpl) bind DoThing::class`.
5. If a new datasource method is needed: add to the interface in `data/source/.../datasource/`, implement in `data/.../datasource/<X>DataSourceImpl.kt` (`internal class ... : DataSource(), XDataSource`, queries wrapped in `dbQuery { }`, Exposed v1 DSL, `Uuid.toJavaUuid()` for ids). New datasources are registered in `data/.../di/`: `singleOf(::XDataSourceImpl) bind XDataSource::class`. New tables go in `data/.../orm/{table,entity}` and must be added to the service's `<Svc>Migration.kt` `tables()` array.
6. Write the impl unit test (see Testing) — every `Error` case + success + event publication if any.

## Recipe: new Ktor route

1. Add a typed resource to `route/<Svc>Routing.kt` (nested `@Resource` classes with `val parent: X = X()` chain):
```kotlin
@Resource("/{id}/cancel")
class Cancel(val parent: Appointment = Appointment(), val id: Uuid)
```
2. Add/extend a `fun Routing.xyz()` in `route/api/<Thing>Route.kt`. Protected routes go inside `authenticate { }`; inject operations lazily inside the handler; request-only DTOs are `@Serializable internal class` at the top of the route file:
```kotlin
fun Routing.thing() {
    authenticate {
        /**
         * Summary: Create thing
         * Description: Create new thing from request
         * Tag: thing
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.svc.microservice.route.api.ThingRequest]
         * Response: 200 application/x-protobuf [com.bookk.svc.domain.api.entity.Thing] Created thing
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create thing errors:
         *  - THING_EXISTS (Code 300010): Thing already exists
         */
        post<Api.Thing> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<ThingRequest>()
            val doThing by application.inject<DoThing>()

            call.respondWith(doThing(userId = principal.userId, ...))
        }
    }
}
```
3. KDoc OpenAPI rules (the ktor openApi plugin parses these): `Summary:`, optional `Description:`, `Tag:`, `Security: jwt` only when inside `authenticate {}`, **`Body:` (NEVER `RequestBody:`)**, one `Response:` line per status. Fully qualified type names in brackets. Every case of the operation's `sealed interface Error` must appear under the 422 response with `NAME (Code <n>): message`. 204 responses: `Response: 204 application/x-protobuf <description>` (no type).
4. When a path id duplicates a body id, validate: `if (it.id != body.id) call.respond(HttpStatusCode.BadRequest, "Invalid request") else ...`.
5. Register the new route fn in `route/<Svc>Route.kt` aggregator (`fun Routing.<svc>Route()`); the aggregator is already wired in `<Svc>Microservice.kt`.
6. `AppPrincipal` fields: `authId`, `userId`, `deviceId` (all `Uuid`).

## Testing

Run: `./gradlew :service:<svc>:microservice:test` / `:service:<svc>:domain:impl:test` (append `--tests "com.bookk...ClassName"` to filter).

Hard rules (enforced by fixtures or review):
- NEVER `runBlocking`. Operation tests: `runUnitTest { }`; route tests: `routeTest { }` (both from core fixtures).
- `given()` / `whenn()` / `then()` markers are **required in every test** — `runUnitTest` asserts at runtime that all three were called; a test without them fails.
- Fresh SUT and fresh mocks per test — no sharing across tests, no class-level mocks.
- DO NOT EDIT `core/src/testFixtures/kotlin/com/bookk/core/test/Test.kt`.
- Test names: backticked sentences, e.g. `` fun `should return failure when request overlaps with existing appointment`() ``.
- Use entity `stub()` factories instead of hand-built instances; pass only the fields the test depends on. Provide real entity instances to mocks (ProtoBuf serialization NPEs otherwise).
- JUnit assertions (`org.junit.jupiter.api.Assertions`). Assert error types with `is`: `assertTrue(result.exceptionOrNull() is DoThing.Error.ThingExists)`.
- If the operation sends events: include a test with `coVerify(exactly = 1) { eventProducer.send(any(SvcEvent.X::class), any()) }`.

### Operation (domain/impl) test template

```kotlin
internal class DoThingImplTest {

    private class SutFixture {
        val thingDataSource = mockk<ThingDataSource>()
        val permissionsDataSource = mockk<PermissionsDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = DoThingImpl(thingDataSource, permissionsDataSource, transactionManager)
    }

    @Test
    fun `should create thing successfully`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val thing = Thing.stub(userId = userId)
        val fixture = SutFixture()
        with(fixture) {
            coEvery { permissionsDataSource.getPermissions(userId, thing.businessId) } returns ObjectPermission.EDIT.int
            coEvery { thingDataSource.create(any()) } returns thing
            transactionManager.mockTransaction() // testFixtures(projects.core.domain.datasource)
        }

        whenn()
        val result = fixture.sut.invoke(userId, thing)

        then()
        assertTrue(result.isSuccess)
        assertEquals(thing, result.getOrNull())
    }
}
```
Permission-denied case: stub `getPermissions` to return `ObjectPermission.READ.int`, assert `result.exceptionOrNull() is Error.OperationNotAllowed`.

### Route (microservice) test template

```kotlin
internal class CreateThingTest {

    @Test
    fun `should create thing successfully`() = routeTest {
        given()
        val useCase: DoThing = mockk()
        val userId = Uuid.random()
        val thing = Thing.stub(userId = userId)
        coEvery { useCase.invoke(userId, any()) } returns Result.success(thing)

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { context ->
                            context.principal(AppPrincipal(Uuid.random(), userId, Uuid.random()))
                        }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { thing() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(SvcRouting.Api.Thing()) { setBody(ThingRequest(...)) }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
```
- Domain-error case: `coEvery { ... } returns Result.failure(DoThing.Error.ThingExists())`, then assert status 422 and `response.body<SimpleServerError>().errorCode == SvcErrorCodes.THING_EXISTS`.
- Unauthorized case: `extension = { install(Authentication) { bearer { authenticate { null } } } }`, assert 401. Omit `Security` mocking nothing else.
- Permission-denied surfaces as **404** through `respondWith` — assert 404, not 403.
- Requests use typed resources (`client.post(SvcRouting.Api.Thing())`), never string paths. `createTestClient()` already sets ProtoBuf content type.
- Fixtures come from: `testImplementation(testFixtures(projects.core))` (given/whenn/then, runUnitTest), `testFixtures(projects.core.service)` (routeTest, setupApplication, createTestClient), `testFixtures(projects.core.domain.datasource)` (mockTransaction). Add `libs.joda.money` if entities use Money. Check the module's `build.gradle.kts` before adding — appointments modules already have these.

### Mandatory coverage (contract-first)

Before writing code, enumerate and track as a checklist:
1. Happy path.
2. EVERY case of the operation's `sealed interface Error` — at BOTH levels: impl test (exception type) and route test (HTTP status + `SimpleServerError.errorCode`).
3. Auth: 401 unauthenticated; permission-denied (impl: `Error.OperationNotAllowed`; route: 404).
4. Event publication, if the operation sends events.

Finish by emitting a Final Coverage Report mapping every checklist item to its test:
```
### Final Coverage Report
- [x] ThingExists: impl exception test + route 422/THING_EXISTS test
- [x] Unauthorized: route 401 test
```
Report any gaps explicitly — never silently skip a case.

---

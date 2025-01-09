import com.book.auth.domain.api.entity.CreateAccountRequest
import com.book.auth.domain.api.routing.AuthRouting
import com.book.auth.microservice.route.api.postSignUpChallenge
import com.book.core.service.installNegotiation
import com.bookk.core.test.createTestClient
import com.bookk.core.test.installTestPlugins
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.ktor.plugin.Koin

class CreateSignUpChallengeTest {
    @Test
    fun test() = testApplication {
        application {
            installTestPlugins()
            install(Koin) {

            }
            routing {
                installNegotiation()
                postSignUpChallenge()
            }
        }
        val client = createTestClient()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Challenge()) {
            setBody(CreateAccountRequest("test", "test", "test"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
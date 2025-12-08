package id.web.hangga

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExternalServiceSimulatedTest {

    @Test
    fun `service handles simulated rate limit`() = testApplication {
        application {
            routing {
                get("/price") {
                    call.respondText(
                        "rate-limit",
                        status = HttpStatusCode.TooManyRequests
                    )
                }
            }
        }

        // Using client testApplication
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                io.ktor.serialization.kotlinx.json.DefaultJson
            }
        }

        runBlocking {
            try {
                // calling route test
                val response = client.get("/price")
                assertEquals(response.status, HttpStatusCode.TooManyRequests)
                val body = response.bodyAsText()
                assertTrue(body.contains("rate-limit"))
            } catch (e: Exception) {
                throw AssertionError("Request should succeed against test server: ${e.message}")
            }
        }
    }
}

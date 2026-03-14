package id.web.hangga.routes

import id.web.hangga.AccountRepository
import id.web.hangga.service.TransferRequest
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import id.web.hangga.stream.PriceFlow
import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

object PriceRoutes {

    fun register(
        routing: Routing,
        service: ExternalCryptoService,
        cache: FallbackCache,
        cbConfig: CircuitBreakerConfigWrapper,
        priceFlow: PriceFlow
    ) {

        routing.get("/price") {
            val price = try {
                service.fetchPriceWithCircuitBreaker(cbConfig)
            } catch (_: Exception) {
                // fallback to cache if external fails
                cache.get() ?: CryptoPrice("bitcoin", "usd", 0.0, "fallback")
            }
            call.respond(price)
        }

        routing.get("/stream/price") {
            // SSE headers
            call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                priceFlow.priceFlow().collect { item ->
                    val json = Json.encodeToString(item)
                    write("data: $json\n\n")
                    flush()
                }
            }
        }

        routing.post("/transfer") {
            try {
                val req = call.receive<TransferRequest>()

                val from = AccountRepository.find(req.from)
                val to = AccountRepository.find(req.to)

                if (from == null || to == null) {
                    call.respond(HttpStatusCode.NotFound, "Account not found")
                    return@post
                }

                thread {
                    from.transfer(to, req.amount)
                }

                call.respond(HttpStatusCode.Accepted, "Transfer request submitted")

            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")

            } catch (ex: kotlinx.serialization.SerializationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            }
        }

        routing.post("/simulatedeadlock") {

            val account1 = AccountRepository.find("19000123")!!
            val account2 = AccountRepository.find("19000234")!!

            val latch = CountDownLatch(1)

            thread {
                latch.await()
                account1.transfer(account2, 100)
            }

            thread {
                latch.await()
                account2.transfer(account1, 200)
            }

            latch.countDown()

            call.respond("Deadlock scenario triggered")
        }
    }
}

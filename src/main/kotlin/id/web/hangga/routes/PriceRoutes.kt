package id.web.hangga.routes

import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import id.web.hangga.stream.PriceFlow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

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
    }
}

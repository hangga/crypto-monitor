package id.web.hangga

import com.typesafe.config.ConfigFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.routes.PriceRoutes
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import id.web.hangga.stream.PriceFlow

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }

    val config = ConfigFactory.load()

    println("IKIIII-->"+config.toString())

    val apiUrl = config.getString("ktor.crypto.externalApiUrl")
    val pollMs = config.getLong("ktor.crypto.pollIntervalMs")
    // --- Baca konfigurasi crypto dari application.conf ---
//    val cryptoConfig = environment.config.config("ktor.crypto")
//    val apiUrl = cryptoConfig.property("externalApiUrl").getString()
//    val apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd"
//    val pollMs = 2000L//cryptoConfig.property("pollIntervalMs").getString().toLong()
//    val pollMs = cryptoConfig.property("pollIntervalMs").getString().toLong()

    // --- CircuitBreaker ---
    val cbWrapper = CircuitBreakerConfigWrapper.createDefault()

    // --- Fallback cache ---
    val cache = FallbackCache.instance

    // --- External service ---
    val clientService = ExternalCryptoService(apiUrl)

    // --- Price flow ---
    val priceFlow = PriceFlow(clientService, cache, cbWrapper, pollMs)

    // --- Routing ---
    routing {
        get("/") {
            call.respondText("Crypto Monitor running - see /price and /stream/price")
        }

        // Register price routes
        PriceRoutes.register(this, clientService, cache, cbWrapper, priceFlow)
    }
}

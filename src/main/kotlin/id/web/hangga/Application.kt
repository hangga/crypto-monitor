package id.web.hangga

import id.web.hangga.config.CryptoConfig
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.routes.PriceRoutes
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import id.web.hangga.stream.PriceFlow
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080

    embeddedServer(Netty, port = port, module = Application::module)
        .start(wait = true)
}

private val logger = LoggerFactory.getLogger("StartupLogger")

fun Application.module() {
    val bootStart = System.currentTimeMillis()

    environment.monitor.subscribe(ApplicationStarted) {
        val duration = System.currentTimeMillis() - bootStart
        logger.info("Application fully started in $duration ms")
    }

    install(ContentNegotiation) { json() }

    val config = CryptoConfig.load()

    val cb = CircuitBreakerConfigWrapper.createDefault()
    val cache = FallbackCache.instance
    val externalService = ExternalCryptoService(config.apiUrl)
    val priceFlow = PriceFlow(
        externalService, cache, cb, config.pollIntervalMs
    )

    // --- CircuitBreaker ---
    val cbWrapper = CircuitBreakerConfigWrapper.createDefault()

    // --- External service ---
    val clientService = ExternalCryptoService(config.apiUrl)

    // --- Routing ---
    routing {
        get("/") {
            call.respondText("Crypto Monitor running - see /price and /stream/price")
        }

        // Register price routes
        PriceRoutes.register(this, clientService, cache, cbWrapper, priceFlow)
    }
}

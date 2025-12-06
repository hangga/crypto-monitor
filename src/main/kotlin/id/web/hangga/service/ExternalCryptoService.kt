package id.web.hangga.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateCallable
import java.util.concurrent.Callable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.double

class ExternalCryptoService(private val apiUrl: String?) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val log = LoggerFactory.getLogger(ExternalCryptoService::class.java)

    suspend fun fetchPrice(): CryptoPrice {
        if (apiUrl == null) {
            log.warn("External API URL is null")
            throw IllegalStateException("api-url-null")
        }

        val response: HttpResponse = client.get(apiUrl)
        val body = response.body<String>()
        val parsed = Json.parseToJsonElement(body).jsonObject
        val price = parsed["bitcoin"]?.jsonObject?.get("usd")?.jsonPrimitive?.double
            ?: throw IllegalStateException("price-not-found")
        return CryptoPrice(id = "bitcoin", currency = "usd", price = price, source = "external")
    }

    // versi stabil dengan CircuitBreaker untuk Kotlin 2.x
    suspend fun fetchPriceWithCircuitBreaker(cbWrapper: CircuitBreakerConfigWrapper): CryptoPrice {
        val callable = Callable {
            runBlocking {
                fetchPrice()
            }
        }

        val decorated: Callable<CryptoPrice> = decorateCallable(cbWrapper.breaker, callable)

        return try {
            decorated.call()
        } catch (e: CallNotPermittedException) {
            log.warn("Circuit breaker is open, returning cached value")
            throw e
        } catch (e: Exception) {
            log.warn("fetchPrice failed: ${e.message}")
            throw e
        }
    }
}

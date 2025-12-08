package id.web.hangga

import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.ExternalCryptoService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

class ChaosTest {

    @Test
    fun `flow survives random failures and fallback cache`() = runBlocking {
        val service = mockk<ExternalCryptoService>()
        val cb = CircuitBreakerConfigWrapper.createDefault()
        val cache = id.web.hangga.service.FallbackCache.instance

        val fallbackPrice = CryptoPrice("bitcoin", "usd", 999.0)
        cache.update(fallbackPrice)

        coEvery { service.fetchPrice() } answers {
            if (Random.nextInt(100) < 70) throw RuntimeException("random-failure")
            else CryptoPrice("bitcoin", "usd", 123.0)
        }

        repeat(50) {
            val price = try {
                cb.execute { service.fetchPrice() }
            } catch (_: Exception) {
                cache.get()!!
            }
            assertNotNull(price)
        }
    }
}

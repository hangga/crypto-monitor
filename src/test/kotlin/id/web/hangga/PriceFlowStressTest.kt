package id.web.hangga

import app.cash.turbine.test
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PriceFlowStressTest {

    @Test
    fun `flow emits fallback when service fails rapidly`() = runTest {
        val service = mockk<ExternalCryptoService>()
        val cb = CircuitBreakerConfigWrapper.createDefault()
        val cache = FallbackCache.instance

        val fallbackPrice = CryptoPrice("bitcoin", "usd", 777.0)
        cache.update(fallbackPrice)

        coEvery { service.fetchPrice() } throws RuntimeException("rapid-failure")

        val flow = id.web.hangga.stream.PriceFlow(service, cache, cb, pollMs = 5).priceFlow()

        flow.test {
            val emitted = awaitItem()
            assertEquals(fallbackPrice.price, emitted.price)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

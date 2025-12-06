package id.web.hangga.stream

import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PriceFlow(
    private val service: ExternalCryptoService,
    private val cache: FallbackCache,
    private val cbConfig: CircuitBreakerConfigWrapper,
    private val pollMs: Long = 2000L
) {
    fun priceFlow(): Flow<CryptoPrice> = flow {
        while (true) {
            try {
                val p = service.fetchPriceWithCircuitBreaker(cbConfig)
                // update cache
                cache.update(p)
                emit(p)
            } catch (_: Exception) {
                val last = cache.get()
                if (last != null) {
                    emit(last.copy(source = "fallback"))
                } else {
                    emit(CryptoPrice(id = "bitcoin", currency = "usd", price = 0.0, source = "fallback"))
                }
            }
            delay(pollMs)
        }
    }
}

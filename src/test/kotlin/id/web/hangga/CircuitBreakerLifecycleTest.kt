package id.web.hangga

import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitBreakerLifecycleTest {

    @Test
    fun `circuit breaker opens after repeated failures`() = runBlocking {
        val cbWrapper = CircuitBreakerConfigWrapper.createDefault()
        val service = mockk<id.web.hangga.service.ExternalCryptoService>()

        assertEquals(State.CLOSED, cbWrapper.breaker.state)

        // Simulating failure
        coEvery { service.fetchPrice() } throws RuntimeException("fail")
        repeat(5) {
            try {
                cbWrapper.execute { service.fetchPrice() }
            } catch (_: Exception) {
            }
        }

        // Should be OPEN after repeated failures
        assertEquals(State.OPEN, cbWrapper.breaker.state)
    }
}

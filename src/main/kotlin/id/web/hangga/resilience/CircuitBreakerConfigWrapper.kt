package id.web.hangga.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import java.time.Duration

class CircuitBreakerConfigWrapper private constructor(
    val breaker: CircuitBreaker
) {
    companion object {
        fun createDefault(): CircuitBreakerConfigWrapper {
            val config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .slidingWindowSize(4)
                .build()
            return CircuitBreakerConfigWrapper(CircuitBreaker.of("cryptoApiCB", config))
        }
    }

    suspend fun <T> execute(block: suspend () -> T): T {
        return breaker.executeSuspendFunction(block)
    }
}

package id.web.hangga.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as R4jConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import java.time.Duration

class CircuitBreakerConfig private constructor(val circuitBreaker: CircuitBreaker) {

    companion object {
        fun createDefault(): CircuitBreakerConfig {
            val config = R4jConfig.custom()
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(3)
                .build()

            val registry = CircuitBreakerRegistry.of(config)
            val cb = registry.circuitBreaker("externalApi")

            return CircuitBreakerConfig(cb)
        }
    }
}
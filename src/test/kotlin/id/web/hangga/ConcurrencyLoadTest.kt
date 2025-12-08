package id.web.hangga

import id.web.hangga.service.CryptoPrice
import id.web.hangga.service.FallbackCache
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertNotNull

class ConcurrencyLoadTest {

    @Test
    fun `cache handles concurrent updates safely`() = runBlocking {
        val cache = FallbackCache.instance

        // Reset cache
        cache.update(CryptoPrice("bitcoin", "usd", 0.0))

        // Launch 200 concurrent updates
        val jobs = List(200) {
            launch {
                val price = CryptoPrice("bitcoin", "usd", (1..1000).random().toDouble())
                cache.update(price) // suspend function, safe in coroutine
            }
        }

        // Tunggu semua coroutine selesai
        jobs.joinAll()

        val cachedPrice = cache.get() // suspend function
        assertNotNull(cachedPrice)

        // Opsional: print untuk debugging
        println("Cached price after concurrent updates: $cachedPrice")
    }

}

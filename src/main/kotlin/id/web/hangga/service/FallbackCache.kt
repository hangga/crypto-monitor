package id.web.hangga.service


import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class FallbackCache() {
    private val mutex = Mutex()
    private var last: CryptoPrice? = null


    suspend fun update(price: CryptoPrice) {
        mutex.withLock { last = price }
    }


    suspend fun get(): CryptoPrice? = mutex.withLock { last }


    companion object {
        val instance = FallbackCache()
    }
}
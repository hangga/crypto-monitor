package id.web.hangga

import id.web.hangga.service.CryptoPrice
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CryptoService(
    private val externalApiUrl: String,
    private val pollIntervalMs: Long
) {

    private val client = HttpClient(CIO)
    private val mutex = Mutex()
    private val latestPricesRef = mutableListOf<CryptoPrice>()

    suspend fun startPolling() {
        while (true) {
            try {
                val json = client.get(externalApiUrl).bodyAsText()
                val prices = parseCryptoPrice(json)

                mutex.withLock {
                    latestPricesRef.clear()
                    latestPricesRef.addAll(prices)
                }
            } catch (e: Exception) {
                println("Failed to fetch crypto prices: ${e.message}")
            }

            delay(pollIntervalMs)
        }
    }

    suspend fun getLatestPrices(): List<CryptoPrice> {
        return mutex.withLock { latestPricesRef.toList() }
    }

    private fun parseCryptoPrice(json: String): List<CryptoPrice> {
        val jsonObj = Json.parseToJsonElement(json).jsonObject
        return jsonObj.map { (id, value) ->
            val price = value.jsonObject["usd"]?.jsonPrimitive?.double ?: 0.0
            CryptoPrice(id = id, currency = "usd", price = price)
        }
    }
}

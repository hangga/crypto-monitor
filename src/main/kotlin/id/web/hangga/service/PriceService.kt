package id.web.hangga.service

import kotlinx.coroutines.delay

class PriceService {

    suspend fun getPriceWithCircuitBreaker(id: String, currency: String): CryptoPrice {
        // sementara kita kembalikan dummy price untuk memastikan route berjalan
        delay(200) // simulasi network delay

        return CryptoPrice(
            id = id,
            currency = currency,
            price = (100..500).random().toDouble(), // angka random supaya kelihatan berubah
            source = "demo"
        )
    }
}

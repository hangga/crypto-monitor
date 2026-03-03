package id.web.hangga.config

data class CryptoConfig(
    val apiUrl: String,
    val pollIntervalMs: Long
) {
    companion object {
        fun load(): CryptoConfig {
            return CryptoConfig(
                apiUrl = System.getenv("CRYPTO_API_URL")
                    ?: "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd",

                pollIntervalMs = System.getenv("CRYPTO_POLL_INTERVAL_MS")
                    ?.toLongOrNull()
                    ?: 2000L
            )
        }
    }
}
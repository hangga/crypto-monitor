package id.web.hangga.config

import com.typesafe.config.ConfigFactory

data class CryptoConfig(
    val apiUrl: String,
    val pollIntervalMs: Long
) {
    companion object {
        fun load(): CryptoConfig {
            val config = ConfigFactory.load()

            val apiUrl = config.getString("ktor.crypto.externalApiUrl")
            val pollMs = config.getLong("ktor.crypto.pollIntervalMs")

            return CryptoConfig(
                apiUrl = apiUrl,
                pollIntervalMs = pollMs
            )
        }
    }
}

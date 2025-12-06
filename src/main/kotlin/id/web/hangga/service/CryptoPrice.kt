package id.web.hangga.service

import kotlinx.serialization.Serializable

@Serializable
data class CryptoPrice(
    val id: String,
    val currency: String,
    val price: Double,
    val source: String = "external"
)
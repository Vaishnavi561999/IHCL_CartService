package com.ihcl.cart.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TenderModes(
    val orderId: String? = null,
    val type:String?,
    val tenderMode: TenderMode,
    val tenderModeDetails: MutableList<TenderModeDetails>
)

@Serializable
data class TenderModeDetails(
    var cardNumber: String,
    var cardPin: String,
    var amount : Double
)

enum class TenderMode{ GIFT_CARD, TATA_NEU}
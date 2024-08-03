package com.ihcl.cart.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoyaltyRequest(
    val epicureDetails: EpicureDetails
)
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class EpicureDetails(
    val bankName: String?,
    val epicureType: String,
    val gravityVoucherCode: String? = null,
    val gravityVoucherPin: String? = null,
    val isBankUrl: Boolean,
    val isShareHolder: Boolean,
    val isTata: Boolean,
    val memberShipPurchaseType: String,
    val offerCode:String? = null,
    val offerName:String? = null
)
package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class PriceSummary(
    var totalPrice : Double?,
    var giftCardPrice : Double,
    var neuCoins : Double,
    val voucher : Double,
    var totalPayableAmount : Double
)

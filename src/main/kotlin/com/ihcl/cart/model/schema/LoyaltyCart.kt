package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoyaltyCart(
    var _id: String,
    val items: LoyaltyCartItems,
    var paymentDetails: MutableList<PaymentDetails>,
    var priceSummary : LoyaltyPriceSummary,
    var paymentMethod: String? = null,
    val createdTimestamp: Date = Date(),
    var modifiedTimestamp: Date = Date()
)















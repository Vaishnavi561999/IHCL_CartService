package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cart(
    var _id: String,
    val items: MutableList<CartItems>?,
    var paymentDetails: MutableList<PaymentDetails>?,
    var priceSummary : PriceSummary?,
    var paymentMethod: String? = null,
    val createdTimestamp: Date = Date(),
    var modifiedTimestamp: Date = Date()
)














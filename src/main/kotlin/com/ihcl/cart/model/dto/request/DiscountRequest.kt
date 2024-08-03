package com.ihcl.cart.model.dto.request

import com.ihcl.cart.model.dto.response.TaxInfo
import com.ihcl.cart.model.schema.DailyRates
import kotlinx.serialization.Serializable

@Serializable
data class DiscountRequest(
    val customerHash: String,
    val orderId: String,
    val discountPrices: Map<Int, Pair<Double, Double>>,
    val daily:List<DailyRates>,
    val tax: TaxInfo
)

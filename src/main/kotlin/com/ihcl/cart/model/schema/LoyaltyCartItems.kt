package com.ihcl.cart.model.schema

import com.ihcl.cart.model.dto.request.EpicureDetails

data class LoyaltyCartItems(
    val epicureDetails: EpicureDetails,
)
data class LoyaltyPriceSummary(
    val price: Double,
    val tax: Double,
    val discountPercent: Int,
    val discountPrice: Double,
    val discountTax: Double,
    var totalPrice: Double,
    var neuCoins: Double,
    var totalPayableAmount: Double
)

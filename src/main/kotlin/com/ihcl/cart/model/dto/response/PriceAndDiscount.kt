package com.ihcl.cart.model.dto.response

data class PriceAndDiscount(
    val price: Double,
    val tax: Double,
    val discountPercent: Double,
    val discountPrice: Double,
    val discountTax: Double
)

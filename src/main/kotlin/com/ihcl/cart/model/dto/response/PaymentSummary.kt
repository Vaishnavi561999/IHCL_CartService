package com.ihcl.cart.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PaymentSummary(
    val totalPrice : Double?,
    val giftCardPrice : Double,
    val neuCoins : Double,
    val voucher : Double,
    val totalPayableAmount : Double
)

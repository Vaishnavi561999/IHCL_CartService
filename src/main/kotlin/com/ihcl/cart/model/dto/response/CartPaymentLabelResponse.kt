package com.ihcl.cart.model.dto.response

import kotlinx.serialization.Serializable

data class CartPaymentLabelResponse(
    val paymentLabels: PaymentLabels?,
    val cartDetails:CartResponse
)
@Serializable
data class PaymentLabels(
    var payNow: Boolean = false,
    var payAtHotel: Boolean = false,
    var isInternational: Boolean = false,
    var confirmBooking: Boolean = false,
    var payDeposit: Boolean = false,
    var payFull: Boolean = false,
    var gccRemarks: String? = null,
    var depositAmount: Double? = null
)
@Serializable
data class ErrorMessage(
    val status: Int,
    val message: String
)

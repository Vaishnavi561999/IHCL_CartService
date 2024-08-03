package com.ihcl.cart.model.dto.request

import com.ihcl.cart.model.schema.PaymentDetails

data class UpdateOrderRequest(
    val orderId: String,
    var paymentDetails: MutableList<PaymentDetails>?,
    var payableAmount: Double?,
    var balancePayable: Double?,
    var paymentMethod: String?,
    var isDepositAmount : Boolean
)

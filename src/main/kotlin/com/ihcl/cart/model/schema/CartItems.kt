package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CartItems(
    val category: String,
    val hotel: MutableList<Hotel>,
    var newTotalPrice: Double?,
    var basePrice: Double,
    var tax : Double,
    var totalPrice : Double,
    var payableAmount : Double,
    var modifiedPayableAmount: Double? = null,
    var refundAmount: Double? = null,
    var totalDepositAmount : Double,
    var totalCouponDiscountValue: Double,
    var balancePayable: Double,
    var isDepositAmount : Boolean,
    var modifiedPaymentDetails: ModifiedPayment?
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ModifiedPayment(
    var modifiedBasePrice: Double?,
    var modifiedTax : Double?,
    var modifiedTotalPrice : Double?,
    var modifiedPayableAmount : Double?
)
package com.ihcl.cart.model.dto.response

import com.ihcl.cart.model.schema.DailyRates

data class RoomInfoDetails(
    val roomCost: Double,
    val taxAmount: Tax?,
    val bookingPolicyDescription: String?,
    val cancelPolicyDescription: String?,
    val description: String?,
    val detailedDescription: String?,
    val dailyRates: List<DailyRates>,
    val roomCode: String?,
    val amountWithTaxesFees: Double,
    val cancelPolicyCode: String?,
    val synxisId: String?)

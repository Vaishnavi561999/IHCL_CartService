package com.ihcl.cart.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTenderMode(
    val orderId: String? = null,
    val type:String?,
    val tenderMode: TenderMode,
    val cardNumber: String,
    val amount : Double
)

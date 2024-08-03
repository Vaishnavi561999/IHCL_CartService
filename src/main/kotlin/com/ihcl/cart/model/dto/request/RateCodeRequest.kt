package com.ihcl.cart.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RateCodeRequest(
    val adults: String,
    val children: String,
    val endDate: String,
    val hotelId: String,
    val numRooms: String,
    val startDate: String,
    val rateCode: String
)

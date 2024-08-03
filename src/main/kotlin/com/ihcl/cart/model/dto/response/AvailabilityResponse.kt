package com.ihcl.cart.model.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class AvailabilityResponse(
    val hotelCode: String,
    val hotelId: String,
    val maximumCostAmount: Int,
    val maximumCostPackageCode: String,
    val maximumCostRoomTypeCode: String,
    val minimumCostAmount: Int,
    val minimumCostPackageCode: String,
    val minimumCostRoomTypeCode: String,
    val roomTypes: List<RoomType>
)
@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class RoomType(
    val currencyCode: String,
    val packages: List<Package?>,
    val roomTypeCode: String
)
@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class Package(
    val amount: Double,
    val currencyCode: String,
    val feeAmount: Int,
    val isMaximumCostRoomType: Boolean,
    val isMinimumCostRoomType: Boolean,
    val packageCode: String,
    val roomCount: Int,
    val taxAmount: Int,
    val totalAmount: Int
)
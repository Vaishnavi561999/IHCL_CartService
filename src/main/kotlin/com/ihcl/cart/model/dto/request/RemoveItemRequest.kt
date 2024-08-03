package com.ihcl.cart.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class RemoveItemRequest(
    val hotelId: String,
    val rooms:List<DeleteRooms>,
    val orderId:String
)
@Serializable
data class DeleteRooms(
    val roomId: String,
    val roomNumber: Int,
    val roomType: String,
    val packageCode: String
)
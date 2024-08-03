package com.ihcl.cart.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ModifyBooking(
    val orderId: String,
    val emailId: String,
    val rateFilter: String,
    val memberTier: String,
    var modifyBookingDetails : MutableList<ModifiedRoomInfo>
)

@Serializable
data class ModifiedRoomInfo(
    val roomId: String,
    val roomTypeCode: String,
    val roomName: String,
    val roomNumber: Int,
    var cost: Double,
    val checkIn: String,
    val checkOut: String,
    var children: Int,
    var adult: Int,
    var rateCode: String,
    var currency: String,
    var packageCode: String,
    var isServiceable : Boolean,
    var message: String,
    var packageName: String,
    var roomImgUrl : String,
    val isPackageCode: Boolean? = false
)



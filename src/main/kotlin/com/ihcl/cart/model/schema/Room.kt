package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ihcl.cart.model.dto.response.Tax
import kotlinx.serialization.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Room(
    var isPackage: Boolean?,
    var roomId: String,
    var roomTypeCode: String,
    var roomName: String,
    var roomNumber: Int,
    var children: Int,
    var adult: Int,
    var cost: Double,
    var rateCode: String?,
    var currency: String?,
    var rateDescription: String,
    var roomDescription: String,
    var packageCode: String,
    var isServiceable : Boolean,
    var message: String,
    var packageName: String,
    var roomImgUrl : String?,
    var tax : Tax?,
    var bookingPolicyDescription: String?,
    var daily:List<DailyRates>?,
    var cancelPolicyDescription: String?,
    var description: String?,
    var detailedDescription: String?,
    var checkIn: String?,
    var checkOut: String?,
    var changePrice : Double?,
    var changeTax: Double?,
    var confirmationId: String?,
    var status: String? = null,
    var cancellationId: String? = null,
    var cancellationTime: String? = null,
    var penaltyApplicable: Boolean = false,
    var cancelRemark: String? = null,
    var noOfNights:Int?,
    var couponDiscountValue: Double? = 0.0,
    var modifyBooking: ModifiedRoomDetails?,
    var roomCode: String?,
    var roomDepositAmount: Double = 0.0,
    var grandTotal: Double,
    var paidAmount: Double?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var cancelPayableAmount: Double? = null,
    var cancelRefundableAmount: Double? = null,
    var cancelPolicyCode: String?,
    var createdTimestamp: Date? = Date(),
    var modifiedTimestamp: Date? = Date()
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ModifiedRoomDetails(
    val isPackage: Boolean? = false,
    var roomId: String,
    var roomTypeCode: String,
    var roomName: String,
    var roomNumber: Int,
    var children: Int,
    var adult: Int,
    var cost: Double,
    var rateCode: String,
    var currency: String?,
    var rateDescription: String,
    var roomDescription: String,
    var packageCode: String,
    var isServiceable : Boolean,
    var message: String,
    var packageName: String,
    var roomImgUrl : String,
    var tax : Tax?,
    val daily:List<DailyRates>?,
    var status: String? = null,
    var cancellationId: String? = null,
    var cancellationTime: String? = null,
    var penaltyApplicable: Boolean = false,
    var cancelRemark: String? = null,
    var noOfNights:Int?,
    val bookingPolicyDescription: String?,
    val cancelPolicyDescription: String?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var description: String?,
    var detailedDescription: String?,
    var checkIn: String,
    var checkOut: String,
    var confirmationId: String?,
    var code: String?,
    val grandTotal: Double,
    val paidAmount: Double?,
    var createdTimestamp: Date? = Date(),
    var modifiedTimestamp: Date? = Date()
)
@Serializable
data class DailyRates(
    var date:String? = null,
    var amount:Double? = null,
    var tax:TaxBreakDown? = null
)
@Serializable
data class TaxBreakDown(
    val amount: Double? = null,
    var breakDown: List<BreakDownDetails>? = listOf()
)

@Serializable
data class BreakDownDetails(
    var amount: Double? = null,
    val code: String? = null,
    val name:String?=null
)



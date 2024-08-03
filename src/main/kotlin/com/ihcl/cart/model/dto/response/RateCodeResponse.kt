package com.ihcl.cart.model.dto.response

import com.google.gson.annotations.SerializedName

data class RateCodeResponse(
    val errorCode: String?,
    val hotelId: String?,
    val synxisId: String?,
    val message: String?,
    val roomTypes: List<RoomTypeInfo>?,
    val chargeList: List<ChargeList>?
)
data class ChargeList(
    @SerializedName("name"    ) var name    : String?  = null,
    @SerializedName("code"    ) var code    : String?  = null
)
data class Daily(
    val availableInventory: Int?,
    val date: String?,
    val price: Price?
)

data class Fees(
    val amount: Double?,
    var breakDown: List<BreakDown?>?
)

data class RoomTypeInfo(
    val roomCode: String?,
    val roomContent: RoomContent?,
    val rooms: List<RoomInfo>?
)
data class RoomInfo(
    val rateCode: String?,
    val rateContent: RateContent?,
    val bookingPolicy: ContentBookingPolicy?,
    val cancellationPolicy: ContentCancelPolicy?,
    val cancelPolicy: ContentCancelPolicy?,
    val daily: List<Daily>?,
    val perNight: PerNight?,
    val tax: Tax?,
    val total: Total?
)


data class ContentBookingPolicy(
    val code: String?,
    val allowPay: Boolean?,
    val depositFee: DepositFee?,
    val description: String?,
    val guaranteeLevel: String?,
    val holdTime: String?,
    val refundableStay: String?,
    val requirements: List<String?>?,
    val transactionFeeDisclaimer: String?
)

data class ContentCancelPolicy(
    val code: String?,
    val cancelFeeAmount: CancelFeeAmount?,
    val cancelFeeType: String?,
    val cancelPenaltyDate: String?,
    val cancelTime: String?,
    val cancelTimeIn: Int?,
    val chargeThreshold: String?,
    val chargeType: String?,
    val charges: List<Any?>?,
    val description: String?,
    val lateCancellationPermitted: Boolean?,
    val modificationRestrictions: String?,
    val noShowFeeAmount: NoShowFeeAmount?,
    val noShowFeeType: String?
)
data class DepositFee(
    val amount: Int?,
    val dueDays: Int?,
    val dueTime: String?,
    val dueType: String?,
    val isPrePayment: Boolean?,
    val taxInclusive: Boolean?,
    val type: String?
)
data class CancelFeeAmount(
    val taxInclusive: Boolean,
    val value: Int
)
data class NoShowFeeAmount(
    val taxInclusive: String,
    val value: Int
)

data class PerNight(
    val price : Price?,
)

data class Price(
    val amount: Double?,
    val currencyCode: String?,
    val total: Total?,
    val fees: Fees?,
    val tax: Tax?,
)
data class RoomContent(
    val categoryCode: String?,
    val code: String?,
    val details: Details?,
    val name: String?
)

data class RateContent(
    val categoryCode: String?,
    val code: String?,
    val currencyCode: String?,
    val details: DetailsInfo?,
    val name: String?
)
data class Tax(
    var amount: Double?,
    var breakDown: List<BreakDown>?
)

data class Total(
    val amount: Double?,
    val amountPayAtProperty: Double?,
    val amountPayableNow: Double?,
    val amountWithInclusiveTaxes: Double?,
    val amountWithTaxesFees: Double?
)

data class Details(
    val bedding: List<Bedding>?,
    val `class`: Class?,
    val description: String?,
    val detailedDescription: String?,
    val extraBed: ExtraBed?,
    val featureList: List<Feature>?,
    val guestLimit: GuestLimit?,
    val indicators: Indicators?,
    val size: Size?,
    val viewList: List<View>?
)
data class DetailsInfo(
    val channelAccessOverridesList: List<Any>?,
    val description: String?,
    val detailedDescription: String?,
    val displayDescription: String?,
    val displayName: String?,
    val indicators: IndicatorsInfo?,
    val rateClass: String?
)
data class BreakDown(
    var amount: Double?,
    val code: String?
)

data class Bedding(
    val code: String?,
    val description: String?,
    val isPrimary: Boolean?,
    val quantity: String?,
    val type: String?
)

data class Class(
    val code: String?,
    val description: String?
)

data class ExtraBed(
    val allowed: Boolean?,
    val cost: Int?
)

data class Feature(
    val description: String?,
    val id: String?,
    val otaCode: String?,
    val otaType: String?,
    val sortOrder: Int?
)

data class GuestLimit(
    val adults: Int?,
    val children: Int?,
    val childrenIncluded: Boolean?,
    val guestLimitTotal: Int?,
    val value: Int?
)

data class Indicators(
    val preferred: Boolean?
)

data class Size(
    val max: Int?,
    val min: Int?,
    val units: String?
)
data class View(
    val code: String?,
    val description: String?,
    val isgsdPreferred: Boolean?,
    val otaType: String?
)

data class IndicatorsInfo(
    val breakfastIncluded: Boolean?,
    val preferred: Boolean?
)

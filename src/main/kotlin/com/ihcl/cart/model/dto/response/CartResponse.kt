package com.ihcl.cart.model.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ihcl.cart.model.dto.request.VoucherRedemptionAvailPrivileges
import com.ihcl.cart.model.schema.DailyRates
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

data class CartResponse(
    var cartId: String,
    val items : List<CartResponseItems>?,
    val paymentSummary : PaymentSummary?,
    var paymentDetails: MutableList<PaymentDetailsInfo>?,
    val totalPriceChange: Double?,
    val totalTaxChange: Double?,
    val basePrice: Double?,
    val tax : Double?,
    val totalPrice : Double?,
    val payableAmount : Double?,
    var totalDepositAmount : Double,
    var totalCouponDiscountValue : Double,
    var balancePayable: Double?,
    var isDepositAmount : Boolean,
    val modifiedPaymentDetails: ModifiedPaymentDetails?,
    val modifiedPayableAmount: Double? = null,
    val refundableAmount: Double? = null,
    val paymentMethod: String? = null,
    val createdDate: String?,
    val modifiedDate:  String?,
    var cancelPolicyDescription: String? = null,
    val errorMessage: ErrorMessage? = null
)

data class CartResponseItems(
    val category: String,
    val hotel: MutableList<HotelDetails>?
)

data class HotelDetails(
    val hotelId: String,
    val hotelName: String,
    val hotelAddress: String,
    val pinCode: String,
    val state: String,
    val checkIn: String,
    val checkOut: String,
    val bookingNumber: String?,
    val promoCode:String? = null,
    val promoType: String?,
    val mobileNumber: String? = null,
    val emailId: String? = null,
    val room: MutableList<RoomDetails>,
    val voucherRedemption: VoucherRedemptionAvailPrivileges?,
    var revisedPrice: Double?,
    var grandTotal: Double?,
    var totalBasePrice: Double?,
    var totalTaxPrice: Double?,
    var amountPaid: Double?,
    val country:String?,
    val storeId:String?,
    val hotelSponsorId:String?,
    var synxisId: String?,
    var bookingCancelRemarks: String?,
    var isSeb: Boolean? = false,
    var sebRequestId: String? = null
)

data class RoomDetails(
    val isPackageCode: Boolean? = false,
    val roomId: String,
    val roomType: String,
    val roomName: String,
    val roomNumber: Int,
    val cost: Double,
    var children: Int,
    var adult: Int,
    val rateCode: String?,
    val currency: String?,
    val rateDescription: String,
    val roomDescription: String,
    var isServiceable : Boolean,
    var message: String,
    var tax : TaxInfo?,
    val bookingPolicyDescription: String?,
    val daily:List<DailyRates>?,
    val cancelPolicyDescription: String?,
    val description: String?,
    val detailedDescription: String?,
    var checkIn: String?,
    var checkOut: String?,
    var packageCode: String,
    var packageName: String,
    var roomImgUrl : String?,
    var changePrice: Double?,
    var changeTax: Double?,
    var status: String?,
    var confirmationId: String?,
    var modifyBooking: ModifiedRoom?,
    var roomCode: String?,
    var noOfNights:Int?,
    var roomDepositAmount: Double?,
    val grandTotal: Double,
    var paidAmount: Double?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var cancellationId: String?,
    var cancellationTime: String?,
    var penaltyApplicable: Boolean,
    var cancelRemark: String?,
    var cancelPayableAmount: Double?,
    var cancelRefundableAmount: Double?,
    var couponDiscountValue: Double,
    val createdTimestamp: Date? = Date(),
    var modifiedTimestamp: Date? = Date()
)
@Serializable
data class TaxInfo(
    var amount: Double?,
    val breakDown: List<BreakDownInfo>?
)
@Serializable
data class BreakDownInfo(
    val amount: Double?,
    val code: String?
)

@Serializable
data class ModifiedRoom(
    val isPackageCode: Boolean? = false,
    val roomId: String,
    val roomType: String,
    val roomName: String,
    val roomNumber: Int,
    val cost: Double,
    var children: Int,
    var adult: Int,
    val rateCode: String,
    val currency: String?,
    val rateDescription: String,
    val roomDescription: String,
    var isServiceable : Boolean,
    var message: String,
    var packageCode: String,
    var packageName: String,
    var roomImgUrl : String,
    var tax : TaxInfo,
    val daily:List<DailyRates>?,
    val bookingPolicyDescription: String?,
    val cancelPolicyDescription: String?,
    val description: String?,
    val detailedDescription: String?,
    var checkIn: String,
    var checkOut: String,
    var status: String?,
    var confirmationId: String?,
    var roomCode: String?,
    var noOfNights:Int?,
    var roomDepositAmount: Double?,
    val grandTotal: Double,
    var paidAmount: Double?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var cancellationId: String? = null,
    var cancellationTime: String? = null,
    var penaltyApplicable: String? = null,
    var cancelRemark: String? = null,
    @Contextual
    var createdTimestamp: Date? = Date(),
    @Contextual
    var modifiedTimestamp: Date? = Date()
)

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class ModifiedPaymentDetails(
    var modifiedBasePrice: Double?,
    var modifiedTax : Double?,
    var modifiedTotalPrice : Double?,
    var modifiedPayableAmount : Double?
)
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentDetailsInfo(
    var paymentType: String?,
    var paymentMethod: String?,
    var paymentMethodType: String?,
    var txnGateway: Int?,
    var txnId: String?,
    var ccAvenueTxnId: String?,
    var txnNetAmount: Double?,
    var txnStatus: String?,
    var txnUUID: String?,
    var cardNo: String?,
    var nameOnCard: String?,
    var userId: String?,
    var redemptionId: String?,
    var pointsRedemptionsSummaryId: String?,
    var externalId: String?,
    var cardNumber: String?,
    val cardPin: String?,
    var preAuthCode: String?,
    var batchNumber: String?,
    var approvalCode: String?,
    var transactionId: Int?,
    var transactionDateAndTime: String?,
    var expiryDate:String?
)

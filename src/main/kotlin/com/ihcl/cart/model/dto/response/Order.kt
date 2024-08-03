package com.ihcl.cart.model.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName
import com.ihcl.cart.model.dto.request.VoucherRedemptionAvailPrivileges
import com.ihcl.cart.model.schema.DailyRates
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class Order(
    val orderId: String,
    val customerHash: String,
    val customerEmail : String,
    val customerId: String,
    val customerMobile: String,
    val channel: String,
    val currencyCode: String,
    val discountAmount: Double,
    val basePrice: Double?,
    val taxAmount: Double?,
    val gradTotal: Double,
    val payableAmount: Double,
    val isRefundable: Boolean,
    val orderType: OrderType,
    val transactionId: String?,
    val billingAddress: BillingAddress,
    val offers: List<Offers>,
    val orderLineItems: MutableList<OrderLineItem>,
    var modifyBookingCount: Int,
    var paymentDetails: TransactionDetails,
    val paymentMethod: String,
    var paymentStatus: String,
    var orderStatus: String,
    var transactionType: String?,
    val refundAmount: Double,
    val createdTimestamp: Date = Date(),
    var modifiedTimestamp: Date = Date(),
    var bookingCancelRemarks: String?
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionDetails(
    var transaction_1: MutableList<PaymentDetail>?,
    val transaction_2: MutableList<PaymentDetail>?,
    val transaction_3: MutableList<PaymentDetail>?,
    val transaction_4: MutableList<PaymentDetail>?
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderLineItem(
    val hotel: Hotel?,
    val giftCard: GiftCard?,
    val loyalty:Loyalty?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hotel(
    val addOnDetails: List<AddOnDetail>,
    val address: Address,
    var bookingNumber: String?,
    val category: String,
    val hotelId: String?,
    val invoiceNumber: String,
    val invoiceUrl: String,
    val name: String?,
    val reservationId: String,
    val roomCount:Int,
    val adultCount:Int,
    val childrens:Int,
    var checkIn: String?,
    var checkOut:String?,
    val promoCode:String? = null,
    val promoType:String? = null,
    val mobileNumber: String? = null,
    val emailId: String? = null,
    val rooms: MutableList<Room>?,
    val voucherRedemption: VoucherRedemptionAvailPrivileges?,
    var status: String,
    val specialRequest: String?,
    var totalDepositAmount : Double?,
    var balancePayable: Double?,
    var isDepositAmount : Boolean?,
    var isDepositPaid: Boolean? = false,
    var revisedPrice: Double?,
    var grandTotal: Double,
    var totalBasePrice: Double,
    var totalTaxPrice: Double,
    var amountPaid: Double,
    var payableAmount: Double,
    var refundAmount: Double,
    var oldTotalBasePrice: Double?,
    var oldTotalTaxPrice: Double?,
    var oldGrandTotal: Double?,
    val country: String,
    val storeId: String?,
    val hotelSponsorId:String?,
    var isSeb: Boolean? = false,
    var sebRequestId: String? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentDetail(
    var paymentType: String?,
    var paymentMethod: String?,
    var paymentMethodType: String?,
    var txnGateway: Int?,
    var txnId: String?,
    var ccAvenueTxnId: String?,
    var txnNetAmount: Double?,
    var txnStatus: String?,
    var txnUUID: String?,
    var cardNumber: String?,
    var preAuthCode: String?,
    var batchNumber: Int?,
    var approvalCode: String?,
    var transactionId: Int?,
    var transactionDateAndTime: String?,
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Offers(
    val offerAmount: Double,
    val offerName: String,
    val offerType: String
)
@Serializable
data class Loyalty(
    val memberCardDetails: MemberCardDetails,
    val membershipDetails: MembershipDetails,
    val shareHolderDetails: ShareHolderDetails?,
    val memberShipPurchaseType: String? = null,
    val isBankUrl :Boolean?=  false,
    val isShareHolder :Boolean?=  false,
    val gravityVoucherCode :String?,
    val gravityVoucherPin :String?,
)
@Serializable
data class MemberCardDetails(
    val enrolling_location: String,
    val enrolling_sponsor: Int,
    val enrollment_channel: String,
    val enrollment_touchpoint: Int,
    val extra_data: ExtraData,
    val epicure_price:Double,
    val taxAmount: Double,
    val addOnCardDetails: AddOnCardDetails?
)
@Serializable
data class ExtraData(
    val country_code: String,
    val domicile: String,
    val epicure_type: String,
    val state: String,
    val country:String,
    val gstNumber:String
)
@Serializable
data class MembershipDetails(
    val memberId: String,
    val mobile: String,
    val user: User,
    val addOnCardDetails: AddOnCardDetails?
)
@Serializable
data class ShareHolderDetails(
    val membershipPlanName: String?,
    val membershipPlanCode: String?,
    val membershipPlanType: String?,
    val bankName: String?
)
@Serializable
data class User(
    val email: String,
    val first_name: String,
    val last_name: String,
    val gender:String?,
    val salutation:String,
    val date_of_birth:String,
    val address: String,
    val pincode:String
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class BillingAddress(
    val address1: String,
    val address2: String,
    val address3: String,
    val city: String,
    val country: String,
    val firstName: String,
    val lastName: String,
    val pinCode: String,
    val state: String,
    val phoneNumber: String,
    val countyCodeISO: String
)
@Serializable
data class GiftCard(
    val deliveryMethods: DeliveryMethodsDto,
    val quantity: Int,
    val giftCardDetails: List<GiftCardDetailsDto>?,
    val promoCode: String? = null,
    val receiverAddress: ReceiverAddressDto?,
    val receiverDetails: ReceiverDetailsDto?,
    val senderDetails: SenderDetailsDto?
)
@Serializable
data class GiftCardDetailsDto(
    var amount: Double?,
    val sku: String?,
    val type: String?,
    val theme: String?,
    var cardNumber: String?,
    var cardPin: String?,
    var cardId: String?,
    var validity: String?,
    var orderId: String?
)
@Serializable
data class ReceiverAddressDto(
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val country: String,
    val pinCode: String,
    val state: String
)
@Serializable
data class ReceiverDetailsDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val message: String,
    val phone: String,
    val rememberMe: Boolean,
    val scheduleOn: String
)
@Serializable
data class SenderDetailsDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val registerAsNeuPass: Boolean
)
@Serializable
data class DeliveryMethodsDto(
    val phone: String,
    val sms: Boolean,
    val whatsApp: Boolean
)
@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class AddOnDetail(
    val addOnCode: String,
    val addOnDesc: String,
    val addOnName: String,
    val addOnPrice: Double,
    val addOnType: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class Room(
    val isPackage: Boolean?,
    var confirmationId: String?,
    var cancellationId: String?,
    var status : String?,
    val addOnDetails: List<AddOnDetail>,
    val checkIn: String,
    val checkOut: String,
    val taxAmount: Double?,
    val tax: TaxInfo?,
    val bookingPolicyDescription: String?,
    val daily:List<DailyRates>?,
    val cancelPolicyDescription: String?,
    val description: String?,
    val detailedDescription: String?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var cancellationTime: String?,
    var penaltyApplicable: Boolean,
    var cancelRemark: String?,
    val discountAmount: Double,
    val discountCode: String,
    var isModified: Boolean,
    val isRefundedItem: Boolean,
    val modifiedWith: String,
    val price: Double,
    val rateDescription: String,
    val refundedAmount: String,
    val roomDescription: String,
    val roomId: String,
    val roomName: String,
    val roomNumber: Int,
    val roomType: String,
    val rateCode: String?,
    val packageCode:String?,
    val adult:Int?,
    val children:Int?,
    val packageName: String?,
    val currency: String?,
    val travellerDetails: List<TravellerDetail>?,
    val roomImgUrl: String?,
    val changePrice:Double?,
    val changeTax:Double?,
    val modifyBooking: ModifyBookingDetails?,
    val grandTotal: Double,
    var paidAmount: Double?,
    var roomCode: String?,
    var roomDepositAmount: Double,
    var noOfNights: Int?,
    var cancelPayableAmount: Double?,
    var cancelRefundableAmount: Double,
    var cancelPolicyCode: String,
    @Contextual
    var createdTimestamp: Date? = Date(),
    @Contextual
    var modifiedTimestamp: Date? = Date()
)
@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class TravellerDetail(
    val dateOfBirth: String,
    val address: String,
    val city: String,
    val countryCode: String,
    val customerId: String,
    val customerType: String,
    val email: String,
    val firstName: String,
    val gender: String,
    val gstNumber: String,
    val lastName: String,
    val membershipNumber: String,
    val mobile: String,
    val name: String,
    val secondaryContact: String,
    val state: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class Address(
    val city: String,
    val contactNumber: String,
    val directions: String,
    val landmark: String,
    val lat: String,
    val long: String,
    val mapLink: String,
    val pinCode: String,
    val state: String,
    val street: String
)
@Serializable
data class AddOnCardDetails(
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val salutation:String?,
    val date_of_birth:String?,
    val mobile:String?,
    val mobileCountryCode:String?,
    val obtainAddOnCard:Boolean,
    val card_number:String? = null,
    val relationship_type: String? = null,
    val card_type:String? = null,
    val fulfilment_status:String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class ModifyBookingDetails(
    val isPackage: Boolean?,
    var confirmationId: String?,
    var cancellationId: String?,
    var status : String?,
    val addOnDetails: List<AddOnDetail>,
    val checkIn: String,
    val checkOut: String,
    val taxAmount: Double?,
    val tax: TaxInfo?,
    val daily:List<DailyRates>?,
    val bookingPolicyDescription: String?,
    val cancelPolicyDescription: String?,
    var penaltyAmount: Double?,
    var penaltyDeadLine: String?,
    var cancellationTime: String?,
    var penaltyApplicable: Boolean,
    val description: String?,
    val detailedDescription: String?,
    val discountAmount: Double,
    val discountCode: String,
    var isModified: Boolean,
    val isRefundedItem: Boolean,
    val modifiedWith: String,
    val price: Double,
    val rateDescription: String,
    val refundedAmount: String,
    val roomDescription: String,
    val roomId: String,
    val roomName: String,
    val roomNumber: Int,
    val roomType: String,
    val rateCode: String?,
    val packageCode:String?,
    val adult:Int?,
    val children:Int?,
    val packageName: String?,
    val currency: String?,
    val travellerDetails: List<TravellerDetail>?,
    val roomImgUrl: String?,
    var cancelRemark: String?,
    val grandTotal: Double,
    val paidAmount: Double,
    var noOfNights: Int?,
    var roomCode: String?,
    @Contextual
    var createdTimestamp: Date? = Date(),
    @Contextual
    var modifiedTimestamp: Date? = Date()
)
enum class PaymentStatus {
    FAILED, PENDING, SUCCESS, CANCELLED, REFUND_INITIATED, PARTIAL_REFUND_INITIATED, REFUND_SUCCESSFUL, REFUND_REJECTED, REFUND_FAILED,
    PARTIAL_REFUND_SUCCESSFUL, FULL_REFUND_SUCCESSFUL, CHARGED
}

enum class OrderStatus {
    AWAITING_CONFIRMATION, PENDING, CREATED, ALLOCATED, CANCEL_INITIATED, CANCELLATION_REJECTED, CANCELLED, FAILED, REFUND_INITIATED, REFUND_SUCCESSFUL, REFUND_REJECTED,
    PARTIAL_REFUND_INITIATED, FULL_REFUND_INITIATED, PARTIAL_REFUND_SUCCESSFUL, FULL_REFUND_SUCCESSFUL, SUCCESS
}

enum class OrderType {
    HOTEL_BOOKING, GIFT_CARD_PURCHASE, RESTAURANTS, SPA, RELOAD_BALANCE, MEMBERSHIP_PURCHASE, HOLIDAYS
}
enum class PaymentMethod {
    PAY_ONLINE, PAY_AT_HOTEL
}
enum class GiftCardStatus {
    COMPLETE
}
enum class BookingStatus {
    AWAITING_CONFIRMATION, PENDING, BOOKED, ALLOCATED, CANCEL_INITIATED, CANCELLATION_REJECTED, CANCELLED, FAILED, REFUND_INITIATED, REFUND_SUCCESSFUL, REFUND_REJECTED,
    PARTIAL_REFUND_INITIATED, FULL_REFUND_INITIATED, PARTIAL_REFUND_SUCCESSFUL, FULL_REFUND_SUCCESSFUL, SUCCESS
}



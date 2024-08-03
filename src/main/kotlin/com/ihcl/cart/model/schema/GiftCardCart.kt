package com.ihcl.cart.model.schema

import com.ihcl.cart.model.dto.request.DeliveryMethods
import java.util.*


data class GiftCardCart(
    var _id: String,
    val items: GCItems?,
    var paymentDetails: MutableList<PaymentDetails>?,
    var priceSummary: GiftCardPriceSummary?,
    val createdTimestamp: Date = Date(),
    var modifiedTimestamp: Date = Date()
)


data class GCItems(
    val isMySelf:Boolean,
    val deliveryMethods: DeliveryMethods?,
    val category: String?,
    val quantity: Int?,
    val giftCardDetails: List<GCDetails?>?,
    val receiverAddress: ReceiverAddress?,
    val senderAddress: ReceiverAddress?,
    val receiverDetails: ReceiverDetails?,
    val senderDetails: SenderDetails?
)

data class ReceiverAddress(
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val country: String?,
    val pinCode: String?,
    val state: String?
)

data class ReceiverDetails(
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val message: String?,
    val phone: String?
)

data class SenderDetails(
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val phone: String?
)

data class GCDetails(
    val amount: Double?,
    val sku: String?,
    val type: String?,
    val theme: String? = null,
    val giftCardNumber: String?,
    val giftCardPin: String?,
    val validity: String?,
    val orderId: String?
)

data class GiftCardPriceSummary(
    var totalPrice: Double?,
    var neuCoins: Double?,
    var totalPayableAmount: Double?
)
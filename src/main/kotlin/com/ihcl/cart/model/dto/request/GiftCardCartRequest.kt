package com.ihcl.cart.model.dto.request

import com.ihcl.cart.model.schema.ReceiverAddress
import com.ihcl.cart.model.schema.ReceiverDetails
import com.ihcl.cart.model.schema.SenderDetails

data class GiftCardCartRequest(
    val orderId: String? = null,
    val self: Boolean,
    val deliveryMethods: DeliveryMethods,
    val giftCardDetails: GiftCardDetails,
    val receiverAddress: ReceiverAddress,
    val senderAddress: ReceiverAddress,
    val receiverDetails: ReceiverDetails,
    val senderDetails: SenderDetails
)

data class DeliveryMethods(
    val phone: String?,
    val email: Boolean?,
    val smsAndWhatsApp: Boolean?
)

data class GiftCardDetails(
    val amount: Double,
    val quantity: Int,
    val sku: String,
    val type: String,
    val theme: String? = null
)
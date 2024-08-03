package com.ihcl.cart.model.dto.response

import com.ihcl.cart.model.dto.request.DeliveryMethods
import com.ihcl.cart.model.schema.*

data class GiftCardCartResponse(
    var _id: String?,
    val items: GCCartResponseItems?,
    var paymentDetails: MutableList<PaymentDetails>?,
    var priceSummary: GiftCardPriceSummary?
)

data class GCCartResponseItems(
    val isMySelf:Boolean?,
    val category: String?,
    val quantity: Int?,
    val giftCardDetails: List<GCDetails?>?,
    val deliveryMethods: DeliveryMethods?,
    val receiverAddress: ReceiverAddress?,
    val senderAddress: ReceiverAddress?,
    val receiverDetails: ReceiverDetails?,
    val senderDetails: SenderDetails?
)
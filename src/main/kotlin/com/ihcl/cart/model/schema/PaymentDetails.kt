package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentDetails(
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
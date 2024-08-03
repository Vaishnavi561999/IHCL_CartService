package com.ihcl.cart.model.dto.request

import com.ihcl.cart.utils.ValidatorUtils.notEmpty
import io.konform.validation.Validation
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTypeReq(
    val customerHash: String,
    val orderId: String,
    val paymentType: String,
    val cardNo: String? = null,
    val nameOnCard: String? = null,
    val expiryDate: String? = null,
    val cardCode: String? = null,
)
val validatePaymentTypeReq = Validation{
    PaymentTypeReq::customerHash required {notEmpty()}
    PaymentTypeReq::orderId required {notEmpty()}
    PaymentTypeReq::paymentType required {notEmpty()}
}

val validatePaymentDetails = Validation{
    PaymentTypeReq::cardNo required {notEmpty()}
    PaymentTypeReq::nameOnCard required {notEmpty()}
    PaymentTypeReq::expiryDate required {notEmpty()}
}
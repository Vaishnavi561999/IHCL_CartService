package com.ihcl.cart.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable
@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class VoucherRedemptionAvailPrivileges(
    var bitDate: String,
    var memberId: String,
    var privileges: String,
    var pin:String? = null,
    var type:String?,
    val isComplementary: Boolean = false,
)
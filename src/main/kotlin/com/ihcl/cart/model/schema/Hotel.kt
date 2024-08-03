package com.ihcl.cart.model.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ihcl.cart.model.dto.request.VoucherRedemptionAvailPrivileges

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hotel(
    var hotelId: String,
    var hotelName: String,
    var hotelAddress: String,
    var pinCode: String,
    var state: String,
    var checkIn: String,
    var checkOut: String,
    val bookingNumber: String?,
    val mobileNumber: String?,
    val emailId: String?,
    var promoCode:String?,
    val promoType: String?,
    var room: MutableList<Room>?,
    var voucherRedemption: VoucherRedemptionAvailPrivileges?,
    var revisedPrice: Double?,
    var grandTotal: Double?,
    var totalBasePrice: Double?,
    var totalTaxPrice: Double?,
    var amountPaid: Double?,
    var country: String?,
    var storeId: String?,
    val hotelSponsorId:String?,
    var synxisId: String? = null,
    var complementaryBasePrice:Double? = null,
    var bookingCancelRemarks: String?,
    var isSeb: Boolean? = false,
    var sebRequestId: String? = null
)
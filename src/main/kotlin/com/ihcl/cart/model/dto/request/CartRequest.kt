package com.ihcl.cart.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ihcl.cart.model.schema.DailyRates
import com.ihcl.cart.utils.ValidatorUtils.notEmpty
import io.konform.validation.Validation
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class CartRequest(
    val category : String,
    val hotel: MutableList<HotelDto>
)

@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class HotelDto(
    val hotelId: String,
    val hotelName: String,
    val hotelAddress: String,
    val pinCode: String,
    val state: String,
    var checkIn: String,
    var checkOut: String,
    val rateFilter: String,
    val memberTier: String,
    val mobileNumber: String?,
    val emailId: String?,
    val room: MutableList<RoomDto>,
    val promoCode: String? = null,
    val promoType: String? = null,
    val country: String?,
    val storeId: String? = null,
    val hotelSponsorId:String?,
    val voucherRedemption: VoucherRedemptionAvailPrivileges?,
    val sebRequestId: String? = null,
    val isSeb: Boolean? = false,
    val isFacilityFeeInclude: Boolean = false
)

val validateHotelDto = Validation{
    HotelDto::hotelId required {notEmpty()}
    HotelDto::hotelName required {notEmpty()}
    HotelDto::hotelAddress required {notEmpty()}
    HotelDto::pinCode required {notEmpty()}
    HotelDto::state required {notEmpty()}
    HotelDto::checkIn required {notEmpty()}
    HotelDto::checkOut required {notEmpty()}
    HotelDto::mobileNumber required {notEmpty()}
    HotelDto::emailId required {notEmpty()}
    HotelDto::country required {notEmpty()}
}
@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class RoomDto(
    val roomId: String,
    val roomTypeCode: String,
    val roomName: String,
    val roomNumber: Int,
    var cost: Double,
    var children: Int,
    var adult: Int,
    var rateCode: String,
    var currency: String,
    var packageCode: String,
    var isServiceable : Boolean,
    var message: String,
    var packageName: String,
    var roomImgUrl : String,
    val isPackageCode: Boolean? = false,
    val daily:List<DailyRates>?
    )




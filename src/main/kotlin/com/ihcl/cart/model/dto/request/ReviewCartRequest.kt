package com.ihcl.cart.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown =  true)
data class ReviewCartRequest( val hotelId: String,
                              val childCount: Int,
                              val adultCount: Int,
                              val promoCode: String,
                              val startDate: String,
                              val endDate: String ,
                              val roomCount: Int)
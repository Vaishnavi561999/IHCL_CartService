package com.ihcl.cart.model.schema

import kotlinx.serialization.Serializable

@Serializable
data class GiftCardValues(
    val _id: String,
    val  giftCardValues:List<CheckGCReloadBalanceDetails>
)
@Serializable
data class CheckGCReloadBalanceDetails(
    val cardType:String,
    val merchant: String?,
    val minReloadLimit:Double,
    val maxReloadLimit:Double,
    val maxReload:Double,
    val sku:String
)
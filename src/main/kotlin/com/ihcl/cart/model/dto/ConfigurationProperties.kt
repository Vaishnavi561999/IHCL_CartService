package com.ihcl.cart.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfigurationProperties(
    val database_url : String,
    val database_name : String,
    val hudiniServiceHost: String,
    val loyaltyServiceHost: String,
    val orderServiceHost : String,
    val epicurePrivilegedNewPrice: String,
    val epicurePrivilegedNewTax: String,
    val epicurePreferredNewPrice: String,
    val epicurePreferredNewTax: String,
    val epicureRenewalDiscount: String,
    val connectionPoolMinSize:Int,
    val connectionPoolMaxSize:Int,
    val epicureFiestaDiscount:Double,
    val dataEncryptionAndDecryptionKey:String,
)

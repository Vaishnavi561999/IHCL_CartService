package com.ihcl.cart.service.v1

import com.ihcl.cart.model.dto.request.GiftCardCartRequest
import com.ihcl.cart.model.dto.response.GiftCardCartResponse
import com.ihcl.cart.model.exception.HttpResponseException
import com.ihcl.cart.repository.CartRepository
import com.ihcl.cart.utils.CART_NOT_FOUND_ERR_MSG
import com.ihcl.cart.utils.Constants.CARD_TYPE_ERROR_MESSAGE
import com.ihcl.cart.utils.Constants.CPG_ID
import com.ihcl.cart.utils.Constants.MAX_RELOAD_LIMIT
import com.ihcl.cart.utils.Constants.MIN_RELOAD_LIMIT
import com.ihcl.cart.utils.Constants.RELOAD_LIMIT_WELLNESS
import com.ihcl.cart.utils.Constants.WELLNESS
import com.ihcl.cart.utils.DataMapperUtils
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GiftCardCartService {
    private val cartRepository by KoinJavaComponent.inject<CartRepository>(CartRepository::class.java)
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun addToCart(customerHash: String, addToCartRequest: GiftCardCartRequest): GiftCardCartResponse {
        val cart = cartRepository.findGiftCardCartByCustomerHash(customerHash)
        val giftCardValues = cartRepository.getGiftCardValues(CPG_ID)
        val checkDetailsList = giftCardValues?.giftCardValues

        checkDetailsList?.find { it.sku == addToCartRequest.giftCardDetails.sku }?.let { checkDetail ->
            if (addToCartRequest.giftCardDetails.amount < checkDetail.minReloadLimit) {
                throw HttpResponseException(MIN_RELOAD_LIMIT, HttpStatusCode.BadRequest)
            }

            if (addToCartRequest.giftCardDetails.amount > checkDetail.maxReloadLimit) {
                if (checkDetail.cardType == WELLNESS) {
                    throw HttpResponseException(RELOAD_LIMIT_WELLNESS, HttpStatusCode.BadRequest)
                } else {
                    throw HttpResponseException(MAX_RELOAD_LIMIT, HttpStatusCode.BadRequest)
                }
            }

            val gcCartDetails = DataMapperUtils.mapDetailsToCartSchema(customerHash, addToCartRequest)
            val gcCartResponse = if (cart == null) {
                log.info("Gift card add to cart request ${addToCartRequest.json}")
                cartRepository.saveGiftCardCart(gcCartDetails)
                DataMapperUtils.prepareGCCartResponse(gcCartDetails)
            } else {
                log.info("updating cart ${cart._id}")
                cartRepository.updateGCCart(customerHash, gcCartDetails)
                DataMapperUtils.prepareGCCartResponse(gcCartDetails)
            }

            log.info("Gift card add to cart response $gcCartResponse")
            return gcCartResponse
        } ?: throw HttpResponseException(CARD_TYPE_ERROR_MESSAGE, HttpStatusCode.BadRequest)
    }
    suspend fun getCart(customerHash: String): GiftCardCartResponse {
        val cart = cartRepository.findGiftCardCartByCustomerHash(customerHash)
        if (cart?.items == null) {
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        }
        return DataMapperUtils.prepareGCCartResponse(cart)
    }
}
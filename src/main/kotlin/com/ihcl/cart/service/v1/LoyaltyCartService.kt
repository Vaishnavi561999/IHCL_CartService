package com.ihcl.cart.service.v1

import com.ihcl.cart.config.Configuration
import com.ihcl.cart.model.dto.request.EpicureDetails
import com.ihcl.cart.model.dto.request.EpicureProperties
import com.ihcl.cart.model.dto.request.LoyaltyRequest
import com.ihcl.cart.model.dto.response.PriceAndDiscount
import com.ihcl.cart.model.exception.HttpResponseException
import com.ihcl.cart.model.schema.LoyaltyCart
import com.ihcl.cart.repository.CartRepository
import com.ihcl.cart.utils.*
import com.ihcl.cart.utils.Constants.CORPORATE
import com.ihcl.cart.utils.Constants.EPICURE_FIESTA
import com.ihcl.cart.utils.Constants.NEW
import com.ihcl.cart.utils.Constants.PREFERRED
import com.ihcl.cart.utils.Constants.PRIVILEGED
import com.ihcl.cart.utils.Constants.RENEWAL
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoyaltyCartService {
    private val cartRepository by KoinJavaComponent.inject<CartRepository>(CartRepository::class.java)
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private val prop = Configuration.env
    private fun calculatePriceAndTax(epicureDetails: EpicureDetails, prop: EpicureProperties): Pair<Double, Double> {
       if ((!epicureDetails.offerName.isNullOrEmpty() && epicureDetails.offerCode.isNullOrEmpty())) {
            throw HttpResponseException(INSUFFICIENT_OFFER_DETAILS, HttpStatusCode.BadRequest)
        }
        if((!epicureDetails.offerName.isNullOrEmpty() && !epicureDetails.offerName.equals(EPICURE_FIESTA, ignoreCase = true) && epicureDetails.offerCode.isNullOrEmpty())){
            throw HttpResponseException(INSUFFICIENT_OFFER_DETAILS, HttpStatusCode.BadRequest)
        }
        return when {
            epicureDetails.epicureType.equals(PRIVILEGED, ignoreCase = true) -> {
                prop.epicurePrivilegedNewPrice to prop.epicurePrivilegedNewTax
            }
            epicureDetails.epicureType.equals(PREFERRED, ignoreCase = true) -> {
                prop.epicurePreferredNewPrice to prop.epicurePreferredNewTax
            }
            epicureDetails.epicureType.equals(CORPORATE, ignoreCase = true) -> {
                if (epicureDetails.isBankUrl && !epicureDetails.bankName.isNullOrEmpty()) {
                    0.0 to 0.0
                } else {
                    throw HttpResponseException(BANK_NAME_NOT_FOUND, HttpStatusCode.BadRequest)
                }
            }
            else -> throw HttpResponseException(INVALID_DETAILS, HttpStatusCode.BadRequest)
        }
    }

    private fun calculateDiscount(discountPercent: Double, price: Double, tax: Double): Pair<Double, Double> {
        val discountPrice = if (discountPercent > 0.0) (discountPercent / 100.0) * price else 0.0
        val discountTax = if (discountPercent > 0.0) (discountPercent / 100.0) * tax else 0.0
        return discountPrice to discountTax
    }

    suspend fun addToCart(customerHash: String, request: LoyaltyRequest): LoyaltyCart {
        log.info("request ${request.json}")
        val price: Double
        val tax: Double
        var discountPercent = 0.0

        val epicureDetails = request.epicureDetails
        val epicureProperties = EpicureProperties(prop.epicurePrivilegedNewPrice.toDouble(), prop.epicurePrivilegedNewTax.toDouble(),
            prop.epicurePreferredNewPrice.toDouble(), prop.epicurePreferredNewTax.toDouble())
        when {
            epicureDetails.memberShipPurchaseType.equals(RENEWAL, ignoreCase = true) &&
                    (!epicureDetails.isTata || !epicureDetails.isShareHolder)||
                    (epicureDetails.memberShipPurchaseType.equals(NEW, ignoreCase = true) &&
                            (epicureDetails.isTata || epicureDetails.isShareHolder)) -> {
                discountPercent = prop.epicureRenewalDiscount.toDouble()
                val (calculatedPrice, calculatedTax) = calculatePriceAndTax(epicureDetails, epicureProperties)
                price = calculatedPrice
                tax = calculatedTax
            }
            ((epicureDetails.memberShipPurchaseType.equals(NEW, ignoreCase = true)) &&
                    (!epicureDetails.isTata || !epicureDetails.isShareHolder) &&
                    epicureDetails.offerName.equals(EPICURE_FIESTA, ignoreCase = true)) -> {
                discountPercent = prop.epicureFiestaDiscount.toDouble()
                val (calculatedPrice, calculatedTax) = calculatePriceAndTax(epicureDetails, epicureProperties)
                price = calculatedPrice
                tax = calculatedTax
            }
            (epicureDetails.memberShipPurchaseType.equals(NEW, ignoreCase = true) &&
                    (!epicureDetails.isTata && !epicureDetails.isShareHolder))-> {
                val (calculatedPrice, calculatedTax) = calculatePriceAndTax(epicureDetails, epicureProperties)
                price = calculatedPrice
                tax = calculatedTax
            }

            else -> throw HttpResponseException(INVALID_DETAILS, HttpStatusCode.BadRequest)
        }

        val (calculatedDiscountPrice, calculatedDiscountTax) = calculateDiscount(discountPercent, price, tax)
        val discountPrice: Double = calculatedDiscountPrice
        val discountTax: Double = calculatedDiscountTax
        val priceAndDiscount = PriceAndDiscount(price, tax, discountPercent, discountPrice, discountTax)
        val cart = DataMapperUtils.createLoyaltyCart(customerHash, request, priceAndDiscount)
        cartRepository.saveLoyaltyCart(cart)
        return cart
    }
    suspend fun getCart(customerHash: String): LoyaltyCart {
        val cart = cartRepository.findLoyaltyCartByCustomerHash(customerHash)
        log.info("cart ${cart?.json}")
        if(cart?.items == null){
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        }
        return cart
    }

    suspend fun emptyCart(customerHash: String): String {
        log.info("$customerHash : removeItems request ${customerHash.json}")
        val cart = cartRepository.findLoyaltyCartByCustomerHash(customerHash)
        if (cart == null) {
            log.info("Items Quantity Mismatch while removing items for customer: $customerHash")
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        } else {
            cartRepository.deleteCartForLoyalty(customerHash)
            log.info("Deleted cart successfully for customer: $customerHash")
        }
        return "SUCCESS"
    }
}
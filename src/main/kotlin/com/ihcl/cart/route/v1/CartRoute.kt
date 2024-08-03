package com.ihcl.cart.route.v1

import com.ihcl.cart.model.dto.request.*
import com.ihcl.cart.model.exception.HttpResponseException
import com.ihcl.cart.service.v1.CartService
import com.ihcl.cart.service.v1.GiftCardCartService
import com.ihcl.cart.service.v1.LoyaltyCartService
import com.ihcl.cart.utils.*
import com.ihcl.cart.utils.Constants.HOTEL_SPONSER_ID_ERROR_MESSAGE
import com.ihcl.cart.utils.Constants.GIFT_CARD_PURCHASE
import com.ihcl.cart.utils.Constants.HOTEL_BOOKING
import com.ihcl.cart.utils.Constants.MEMBERSHIP_PURCHASE
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent

fun Application.configureCartRouting() {
    val cartService by KoinJavaComponent.inject<CartService>(CartService::class.java)
    val giftCardCartService by KoinJavaComponent.inject<GiftCardCartService>(GiftCardCartService::class.java)
    val loyaltyCartService by KoinJavaComponent.inject<LoyaltyCartService>(LoyaltyCartService::class.java)

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Put)
        allowHeader("CUSTOMERHASH")
        allowHeader("TYPE")
        exposeHeader("CUSTOMERHASH")
        exposeHeader("TYPE")

    }

    routing {
        route("/v1") {
            post("/cart/add-to-cart") {
                addToCart(call, cartService)
            }

            get("/cart/fetch-cart") {
                fetchCart(call, cartService, loyaltyCartService, giftCardCartService)
            }

            get("/cart/item-count") {
                getItemCount(call, cartService)
            }

            put("/cart/change-room") {
                updateCart(call, cartService)
            }

            delete("/cart/item-remove") {
                deleteRoom(call, cartService)
            }
            post("/cart/delete-room") {
                deleteNoAvailableRoom(call, cartService)
            }

            delete("/cart/empty-cart") {
                emptyCart(call, cartService, loyaltyCartService)
            }
            put("/cart/merge-cart") {
                mergeCart(call, cartService)
            }

            post("/cart/add-tender-mode") {
                addTenderMode(call, cartService)
            }

            delete("/cart/delete-tender-mode") {
                deleteTenderMode(call, cartService)
            }

            post("/cart/payment-type") {
                val response = cartService.updatePaymentType(call.receive() as PaymentTypeReq)
                call.respond(response)
            }

            post("/cart/coupon-discount") {
                couponDiscount(call, cartService)
            }
        }
        route("/v1/cart/gc") {
            post("/add-to-cart") {
              giftCardAddToCart(call,giftCardCartService, cartService)
            }
        }
        route("/v1/cart/loyalty") {
            post("/add-to-cart") {
                loyaltyAddToCart(call, loyaltyCartService, cartService)
            }
        }
    }
}

suspend fun addToCart(call: ApplicationCall, cartService: CartService) {
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun addToCart(hash: String?) {
        hash?.let {
            val request = call.receive() as CartRequest
            val hotel = request.hotel.first()
            if (!hotel.voucherRedemption?.memberId.isNullOrEmpty() && hotel.hotelSponsorId.isNullOrEmpty()) {
                throw HttpResponseException(HOTEL_SPONSER_ID_ERROR_MESSAGE, BadRequest)
            }
            val response = cartService.addToCart(hash, request)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }

    suspend fun handleAuthenticatedUser(hash: String) {
        addToCart(hash)
    }

    suspend fun handleAnonymousUser() {
        val anonymousUser = cartService.generatingGuestUser()
        call.response.headers.append(GUESTUSER, anonymousUser)
        addToCart(anonymousUser)
    }

    if (customerHash.isNullOrBlank()) {
        handleAnonymousUser()
    } else {
        handleAuthenticatedUser(customerHash)
    }
}

suspend fun fetchCart(
    call: ApplicationCall, cartService: CartService,
    loyaltyCartService: LoyaltyCartService, giftCardCartService: GiftCardCartService
) {
    val customerHash = call.request.headers[CUSTOMERHASH]
    val type = call.request.headers[TYPE]
    suspend fun getCartByHash(hash: String?) {
        when {
            type.equals(HOTEL_BOOKING, ignoreCase = true) -> {
                val response = cartService.getCart(hash.toString())
                call.respond(response)
            }

            type.equals(GIFT_CARD_PURCHASE, ignoreCase = true) -> {
                val response = giftCardCartService.getCart(hash.toString())
                call.respond(response as Any)
            }

            type.equals(MEMBERSHIP_PURCHASE, ignoreCase = true) -> {
                val response = loyaltyCartService.getCart(hash.toString())
                call.respond(response as Any)
            }

            else -> {
                throw HttpResponseException(INVALID_JOURNEY_TYPE, HttpStatusCode.NotFound)
            }
        }
    }
    customerHash?.let {
        getCartByHash(customerHash)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun getItemCount(call: ApplicationCall, cartService: CartService) {
    val customerHash = call.request.headers[CUSTOMERHASH]
    val guestUser = call.request.headers[GUESTUSER]
    suspend fun getItemCount(hash: String?) {
        hash?.let {
            val response = cartService.getCartItemCount(hash)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    customerHash?.let {
        getItemCount(customerHash)
    } ?: guestUser?.let {
        getItemCount(guestUser)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun updateCart(call: ApplicationCall, cartService: CartService) {
    val customerHash = call.request.headers[CUSTOMERHASH]
    val guestUser = call.request.headers[GUESTUSER]
    suspend fun updateCartByHash(hash: String?) {
        hash?.let {
            val response = cartService.updateCart(hash, call.receive() as CartRequest)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    customerHash?.let {
        updateCartByHash(customerHash)
    } ?: guestUser?.let {
        updateCartByHash(guestUser)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun deleteRoom(call: ApplicationCall, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    val guestUser = call.request.headers[GUESTUSER]
    suspend fun removeItemFromCart(hash: String?) {
        hash?.let {
            val response = cartService.removeItem(hash, call.receive() as RemoveItemRequest)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    customerHash?.let {
        removeItemFromCart(customerHash)
    } ?: guestUser?.let {
        removeItemFromCart(guestUser)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }

}

suspend fun deleteNoAvailableRoom(call: ApplicationCall,cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun removeItemFromCart(hash: String?) {
        hash?.let {
            val response =
                cartService.deleteRoomsForNoAvailability(hash, call.receive() as RemoveItemRequest)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    customerHash?.let {
        removeItemFromCart(customerHash)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }

}

suspend fun emptyCart(call: ApplicationCall, cartService: CartService, loyaltyCartService: LoyaltyCartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    val type = call.request.headers[TYPE]

    suspend fun removeItemsFromCart(hash: String?) {
        when {
            type.equals(HOTEL_BOOKING, ignoreCase = true) -> {
                val response = cartService.emptyCart(hash.toString())
                call.respond(response)
            }

            type.equals(GIFT_CARD_PURCHASE, ignoreCase = true) -> {
                val response = cartService.emptyGCCart(hash.toString())
                call.respond(response)
            }

            type.equals(MEMBERSHIP_PURCHASE, ignoreCase = true) -> {
                val response = loyaltyCartService.emptyCart(hash.toString())
                call.respond(response)
            }

            else -> {
                throw HttpResponseException(INVALID_JOURNEY_TYPE, HttpStatusCode.NotFound)
            }
        }
    }
    customerHash?.let {
        removeItemsFromCart(customerHash)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun mergeCart(call: ApplicationCall, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    val guestUser = call.request.headers[GUESTUSER]
    val type = call.request.headers[TYPE]
    if (!customerHash.isNullOrEmpty() && !guestUser.isNullOrEmpty() && !type.isNullOrEmpty()) {
        when {
            type.equals(HOTEL_BOOKING, ignoreCase = true) -> {
                val response = cartService.mergeCart(customerHash, guestUser)
                call.respond(response)
            }

            type.equals(MEMBERSHIP_PURCHASE, ignoreCase = true) -> {
                val response = cartService.mergeCartForLoyalty(customerHash, guestUser)
                call.respond(response)
            }

            type.equals(GIFT_CARD_PURCHASE, ignoreCase = true) -> {
                val response = cartService.mergeCartForGC(customerHash, guestUser)
                call.respond(response)
            }
        }
    } else {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun addTenderMode(call: ApplicationCall, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun addTenderModeByHash(hash: String) {
        val response = cartService.addTenderModes(hash, call.receive() as TenderModes)
        if (response == null) {
            call.fireHttpResponse(BadRequest, INVALID_JOURNEY_TYPE)
        } else {
            call.respond(response)
        }
    }
    customerHash?.let {
        addTenderModeByHash(customerHash)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun deleteTenderMode(call: ApplicationCall, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun deleteTenderModeByHash(hash: String) {
        val response = cartService.deleteTenderMode(hash, call.receive() as DeleteTenderMode)
        if (response == null) {
            call.fireHttpResponse(BadRequest, INVALID_JOURNEY_TYPE)
        } else {
            call.respond(response)
        }
    }
    customerHash?.let {
        deleteTenderModeByHash(customerHash)
    } ?: run {
        call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
    }
}

suspend fun couponDiscount(call: ApplicationCall, cartService: CartService){
    val discountRequest = call.receive() as DiscountRequest
    val cartResponse = cartService.getDiscountPrice(discountRequest)
    call.respond(cartResponse)
}
suspend fun giftCardAddToCart(call: ApplicationCall, giftCardCartService: GiftCardCartService, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun addCart(hash: String?) {
        hash?.let {
            val response = giftCardCartService.addToCart(hash, call.receive() as GiftCardCartRequest)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    if (customerHash.isNullOrBlank()) {
        val anonymousUser = cartService.generatingGuestUser()
        call.response.headers.append(GUESTUSER, anonymousUser)
        addCart(anonymousUser)
    } else {
        customerHash.let {
            addCart(customerHash)
        }
    }
}

suspend fun loyaltyAddToCart(call: ApplicationCall, loyaltyCartService: LoyaltyCartService, cartService: CartService){
    val customerHash = call.request.headers[CUSTOMERHASH]
    suspend fun addCart(hash: String?) {
        hash?.let {
            val response = loyaltyCartService.addToCart(hash, call.receive() as LoyaltyRequest)
            call.respond(response)
        } ?: run {
            call.fireHttpResponse(UnprocessableEntity, CUSTOMERHASH_REQUIRED_ERR_MSG)
        }
    }
    if (customerHash.isNullOrBlank()) {
        val anonymousUser = cartService.generatingGuestUser()
        call.response.headers.append(GUESTUSER, anonymousUser)
        addCart(anonymousUser)
    } else {
        customerHash.let {
            addCart(customerHash)
        }
    }
}






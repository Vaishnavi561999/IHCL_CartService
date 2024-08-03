package com.ihcl.cart.service.v1


import com.ihcl.cart.client.client
import com.ihcl.cart.config.Configuration
import com.ihcl.cart.model.dto.request.*
import com.ihcl.cart.model.dto.response.*
import com.ihcl.cart.model.exception.HttpResponseException
import com.ihcl.cart.model.schema.*
import com.ihcl.cart.model.schema.Room
import com.ihcl.cart.repository.CartRepository
import com.ihcl.cart.utils.*
import com.ihcl.cart.utils.Constants.CC_AVENUE
import com.ihcl.cart.utils.Constants.CHECK_AVAILABILITY_URL
import com.ihcl.cart.utils.Constants.CONFIRM_BOOKING
import com.ihcl.cart.utils.Constants.COUNTRY_CODE
import com.ihcl.cart.utils.Constants.COUPON_PROMO_TYPE
import com.ihcl.cart.utils.Constants.FACILITY
import com.ihcl.cart.utils.Constants.GIFT_CARD
import com.ihcl.cart.utils.Constants.GIFT_CARD_PURCHASE
import com.ihcl.cart.utils.Constants.HOTEL_BOOKING
import com.ihcl.cart.utils.Constants.INITIATED
import com.ihcl.cart.utils.Constants.JUS_PAY
import com.ihcl.cart.utils.Constants.MEMBERSHIP_PURCHASE
import com.ihcl.cart.utils.Constants.PAY_AT_HOTEL
import com.ihcl.cart.utils.Constants.PAY_DEPOSIT
import com.ihcl.cart.utils.Constants.PAY_FULL
import com.ihcl.cart.utils.Constants.PAY_NOW
import com.ihcl.cart.utils.Constants.PAY_ONLINE
import com.ihcl.cart.utils.Constants.PENDING
import com.ihcl.cart.utils.Constants.RATE_CODE_URL
import com.ihcl.cart.utils.Constants.TATA_NEU
import com.ihcl.cart.utils.Constants.UPDATE_ORDER_BOOKING
import com.ihcl.cart.utils.Constants.UPDATE_ORDER_URL
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.streams.toList
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class CartService {
    private val cartRepository by KoinJavaComponent.inject<CartRepository>(CartRepository::class.java)
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private val prop = Configuration.env
    fun generatingGuestUser(): String {
        return ValidatorUtils.getGuestHash()
    }

    suspend fun addToCart(customerHash: String, addToCartRequest: CartRequest): CartPaymentLabelResponse {
        log.info("Add to cart request.. ${addToCartRequest.json}")
        val checkIn = addToCartRequest.hotel.first().checkIn
        val checkOut = addToCartRequest.hotel.first().checkOut

        addToCartRequest.hotel.first().checkIn = ValidatorUtils.validateDate(checkIn)
        addToCartRequest.hotel.first().checkOut = ValidatorUtils.validateDate(checkOut)

        ValidatorUtils.validateHotelAddress(addToCartRequest.hotel.first().hotelAddress)

        log.debug("$customerHash :addToCart cart request ${addToCartRequest.json}")

        ValidatorUtils.validateRequestBody(validateHotelDto.validate(addToCartRequest.hotel.first()))
        if (addToCartRequest.hotel.first().voucherRedemption?.isComplementary == true &&
            (addToCartRequest.hotel.first().voucherRedemption?.memberId.isNullOrEmpty())
        ) {
            throw HttpResponseException(VOUCHER_DETAILS_ERR_MSG, HttpStatusCode.BadRequest)
        }
        val rateCodeResponse = rateFilterRateCode(addToCartRequest)
        val roomInfo = findRoomInfo(rateCodeResponse, addToCartRequest.hotel.first().room, addToCartRequest.hotel.first().isFacilityFeeInclude)
        var cart: Cart? = cartRepository.findCartByCustomerHash(customerHash)

        cart = if (cart == null || cart.items?.isEmpty() == true) {
            addInitialProduct(customerHash, addToCartRequest, roomInfo)
        } else {
            addRoomToCart(cart, addToCartRequest, roomInfo)
        }
        val roomSize = cart.items!![0].hotel[0].room!!.size
        var totalBasePrice = 0.0
        var totalTaxPrice = 0.0

        for (i in 0 until roomSize) {
                totalBasePrice += cart.items!![0].hotel[0].room!![i].cost
                totalTaxPrice += cart.items!![0].hotel[0].room!![i].tax?.amount!!
            }
        val countryCode= cart.items?.first()?.hotel?.first()?.country
        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
            totalBasePrice = roundDecimals(totalBasePrice)
            totalTaxPrice = roundDecimals(totalTaxPrice)
        }
        var grandTotal = totalBasePrice + totalTaxPrice
        cart.items?.first()?.hotel?.first()?.grandTotal = grandTotal
        cart.items?.first()?.hotel?.first()?.revisedPrice = grandTotal
        cart.items?.first()?.hotel?.first()?.totalBasePrice = totalBasePrice
        cart.items?.first()?.hotel?.first()?.totalTaxPrice = totalTaxPrice
        val basePriceSum = if(cart.items?.first()?.hotel?.first()?.voucherRedemption?.isComplementary == true &&
            !cart.items?.first()?.hotel?.first()?.voucherRedemption?.memberId.isNullOrEmpty()){
                0.0
            }else {
                totalBasePrice
            }
        grandTotal = basePriceSum + totalTaxPrice
        cart.items?.first()?.basePrice = basePriceSum
        cart.items?.first()?.tax = totalTaxPrice
        cart.items?.first()?.totalPrice = grandTotal
        cart.items?.first()?.payableAmount = grandTotal
        cart.items?.first()?.hotel?.first()?.grandTotal = grandTotal
        cart.items?.first()?.hotel?.first()?.isSeb = addToCartRequest.hotel.first().isSeb
        cart.items?.first()?.hotel?.first()?.sebRequestId = addToCartRequest.hotel.first().sebRequestId
        log.info("cart ${cart.json}")
        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
            cart.paymentDetails?.first()?.txnNetAmount = roundDecimals(cart.items!!.first().payableAmount)
        }else{
            cart.paymentDetails?.first()?.txnNetAmount = cart.items!!.first().payableAmount
        }
        cart.priceSummary?.totalPrice = grandTotal
        cart.priceSummary?.totalPayableAmount = grandTotal
        cartRepository.saveCart(cart)
        log.info("product successfully added for ${cart.json}")
        return getPaymentLabels(cart, true)
    }

    private fun addRoomToCart(cart: Cart,addToCartRequest: CartRequest, roomInfo: RoomInfoDetails): Cart{
        cart.items?.forEach { cartItems ->
            cartItems.hotel.forEach { hotel ->
                addToCartRequest.hotel.forEach { htl ->
                    hotel.voucherRedemption = htl.voucherRedemption
                    if (hotel.hotelId == htl.hotelId) {
                        hotel.hotelName = htl.hotelName
                        hotel.hotelAddress = htl.hotelAddress
                        hotel.checkIn = htl.checkIn
                        hotel.checkOut = htl.checkOut
                        hotel.pinCode = htl.pinCode
                        hotel.state = htl.state
                        hotel.promoCode = htl.promoCode
                        hotel.country = htl.country
                        hotel.storeId = htl.storeId
                        hotel.synxisId = roomInfo.synxisId
                        hotel.room!!.withIndex().find { rm ->
                            rm.value.roomNumber == htl.room[0].roomNumber
                        }?.let {
                            throw HttpResponseException("ACTIVE RESERVATION SESSION DETECTED", HttpStatusCode.NotAcceptable)
                        } ?: run {
                            val room = DataMapperUtils.mapRoomProduct(addToCartRequest, roomInfo)
                            log.info("room details ${room.json}")
                            cartItems.hotel.map {
                                log.info("mapping details $it")
                                it.room!!.addAll(room)
                            }
                        }

                    }else {
                        throw HttpResponseException("ACTIVE RESERVATION SESSION DETECTED", HttpStatusCode.NotAcceptable)
                    }
                }
            }
        }
        return cart
    }
    private fun findRoomInfo(rateCodeResponse: RateCodeResponse, rooms: MutableList<RoomDto>, isFacilityFeeInclude: Boolean): RoomInfoDetails {

        val synxisId = rateCodeResponse.synxisId
        var roomCost = 0.0
        var taxAmount: Tax? = null
        var bookingPolicyDescription: String? = null
        var cancelPolicyDescription: String? = null
        var description: String? = null
        var detailedDescription: String? = null
        var roomCode: String? = null
        var amountWithTaxesFees = 0.0
        var cancelPolicyCode: String? = null
        var dailyRates = listOf<DailyRates>()
        var feeAmount = 0.0


        rooms.forEach { room ->
            val roomType = rateCodeResponse.roomTypes?.find { it.roomCode == room.roomTypeCode }
                ?: throw HttpResponseException(
                    "Room type with code ${room.roomTypeCode} not found",
                    HttpStatusCode.NotFound
                )

            val roomInfo = roomType.rooms?.find { it.rateCode == room.rateCode }
                ?: throw HttpResponseException(
                    "Room with rate code ${room.rateCode} not found",
                    HttpStatusCode.NotFound
                )

            roomCost = roomInfo.total?.amount!!
            bookingPolicyDescription = roomInfo.bookingPolicy?.description
            cancelPolicyDescription = roomInfo.cancellationPolicy?.description
            description = roomInfo.rateContent?.details?.description
            detailedDescription = roomInfo.rateContent?.details?.detailedDescription
            dailyRates = roomInfo.daily?.map {
                val (combinedBreakDown, totalFeeAmount) = taxAndFee(it, rateCodeResponse, isFacilityFeeInclude)
                feeAmount += totalFeeAmount
                val total = combinedBreakDown.sumOf { it.amount ?: 0.0}
                DailyRates(
                    date = it.date,
                    amount = it.price?.amount,
                    tax = TaxBreakDown(
                        amount = total,
                        breakDown = combinedBreakDown
                    )
                )
            } ?: emptyList()
            val feeBreakDown =  listOfNotNull(
                feeAmount.takeIf { it > 0.0 }?.let {
                    BreakDown(
                        amount = it,
                        code = FACILITY
                    )
                }
            )
           taxAmount = if(isFacilityFeeInclude){
               Tax(
                   amount = roomInfo.tax?.amount?.plus(feeAmount),
                   breakDown = roomInfo.tax?.breakDown?.plus(feeBreakDown)
               )
           }else{
               roomInfo.tax
           }
            roomCode = roomInfo.bookingPolicy?.code
            if (roomInfo.perNight != null && roomInfo.bookingPolicy?.code.equals(Constants.GDP1N)) {
                amountWithTaxesFees = roomInfo.perNight.price?.total?.amountWithTaxesFees!!
            }
            cancelPolicyCode = roomInfo.cancellationPolicy?.code

        }

        return RoomInfoDetails(
            roomCost,
            taxAmount,
            bookingPolicyDescription,
            cancelPolicyDescription,
            description,
            detailedDescription,
            dailyRates,
            roomCode,
            amountWithTaxesFees,
            cancelPolicyCode,
            synxisId
        )

    }
    private fun taxAndFee(dailyList: Daily, rateCodeResponse: RateCodeResponse, isFacilityFeeInclude: Boolean): Pair<List<BreakDownDetails>, Double> {

        val taxBreakDown = dailyList.price?.tax?.breakDown?.map { breakdown ->
            val name = rateCodeResponse.chargeList?.find { it.code == breakdown.code }?.name
            BreakDownDetails(
                amount = breakdown.amount,
                code = breakdown.code,
                name = name
            )
        } ?: emptyList()

        val feeBreakDown = if(isFacilityFeeInclude) {
            dailyList.price?.fees?.breakDown?.map { breakdown ->
                val name = rateCodeResponse.chargeList?.find { it.code == breakdown?.code }?.name
                BreakDownDetails(
                    amount = breakdown?.amount,
                    code = breakdown?.code,
                    name = name
                )
            } ?: emptyList()
        }else{
            emptyList()
        }
        val combinedBreakDown = taxBreakDown + feeBreakDown
        val totalFeeAmount = feeBreakDown.sumOf { it.amount ?: 0.0 }

        return Pair(combinedBreakDown, totalFeeAmount)
    }

    //private Methods
    private fun addInitialProduct(
        customerHash: String,
        addToCartRequest: CartRequest,
        roomInfo: RoomInfoDetails
    ): Cart {
        log.info("Adding first product in cart for customer#: $customerHash")
        return DataMapperUtils.mapCartSchema(customerHash, addToCartRequest,roomInfo)
    }

    private suspend fun rateFilterRateCode(cartRequest: CartRequest):RateCodeResponse{
        val rateCodeURL = prop.hudiniServiceHost.plus(RATE_CODE_URL)
        val rateCodeReq = DataMapperUtils.mapRateCodeRequest(cartRequest)
        log.info("Rate code request ${rateCodeReq.json}")
        try {
            val response = client.post(rateCodeURL) {
                timeout {
                    REQUESTED_TIME_OUT.also { requestTimeoutMillis = it.toLong() }
                }
                contentType(ContentType.Application.Json)
                setBody(rateCodeReq)
            }
            val rateCodeRes = response.body<RateCodeResponse>()
            log.info("Rate code response status:: ${response.status }, ${rateCodeRes}")
            if(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted){
                if (rateCodeRes.roomTypes.isNullOrEmpty()) {
                    throw HttpResponseException(RATE_CODE_NOT_FOUND, HttpStatusCode.NotFound)
                } else return rateCodeRes
            }else{
                log.error("Rate code response failed status:: ${response.status}")
                throw HttpResponseException(RATE_CODE_NOT_FOUND, HttpStatusCode.NotFound)
            }

        }catch (httpResponseException: HttpResponseException){
            throw httpResponseException
        }catch (e: Exception){
            log.error("Rate code API failed with an exception ${e.message} and ${e.cause}")
            throw HttpResponseException("$RATE_CODE_NOT_FOUND. ${e.message}", HttpStatusCode.InternalServerError)
        }
    }

   /* private suspend fun modifyBookingRateFiltersRateCode(
        hotelId: String,
        modifyBooking: ModifyBooking,
        room: Int
    ): RateCodeResponse {
        val rateCodeURL = prop.hudiniServiceHost.plus(RATE_CODE_URL)
        val rateCodeReq =
            DataMapperUtils.mapModifyBookingRateCodeRequest(hotelId, modifyBooking, room)
        val response = client.post(rateCodeURL) {
            contentType(ContentType.Application.Json)
            setBody(rateCodeReq)
        }
        log.info("Rate Code response for modify booking${response.bodyAsText()}")
        val rateCodeResponse = response.body<RateCodeResponse>()
        if (rateCodeResponse.roomTypes.isNullOrEmpty()) {
            throw HttpResponseException(rateCodeResponse.message!!, HttpStatusCode.NotFound)
        } else return rateCodeResponse
    }*/


    suspend fun getCart(customerHash: String): Any {
        log.info("$customerHash : get cart request")
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        if ((cart == null) || cart.items!!.isEmpty()) {
            log.info("Cart is empty for customer#: $customerHash")
            return DataMapperUtils.mapCartEmptyResponse(customerHash)
        }
        log.info("Get Cart Details successful for customer#: + $customerHash")
        return if(cart.items.first().hotel.first().promoType.equals(COUPON_PROMO_TYPE, ignoreCase = true)){
            getPaymentLabels(cart, false)
        }else{
            val cartResponse = DataMapperUtils.mapGetCartResponse(cart, null)
            val paymentLabelsInfo = PaymentLabels()
            DataMapperUtils.mapPaymentLabelResponse(paymentLabelsInfo,cartResponse)
        }

    }

    suspend fun mergeCart(customerHash: String, anonymousCustomerHash: String): CartResponse {
        log.info("$customerHash : merge cart request for $anonymousCustomerHash")
        val cart = cartRepository.findCartByCustomerHash(anonymousCustomerHash)
        if(cart?.items.isNullOrEmpty()){
            throw HttpResponseException(GUEST_CART_NOT_FOUND, HttpStatusCode.BadRequest)
        }
        log.info("Get Cart Details successful for anonymousCustomer# for Booking: + $anonymousCustomerHash")
        cartRepository.updateCart(customerHash, cart!!)
        cartRepository.deleteBookingCart(anonymousCustomerHash)
        val updated = cartRepository.findCartByCustomerHash(customerHash)
        return DataMapperUtils.mapGetCartResponse(updated!!, null)
    }
    suspend fun mergeCartForLoyalty(customerHash: String, anonymousCustomerHash: String): LoyaltyCart {
        log.info("$customerHash : merge cart request for $anonymousCustomerHash")
        val loyaltyCart = cartRepository.findLoyaltyCartByCustomerHash(anonymousCustomerHash)?: throw HttpResponseException("Guest cart is empty", HttpStatusCode.BadRequest)
        log.info("Get Cart Details successful for anonymousCustomer# for Loyalty: + $anonymousCustomerHash")
        cartRepository.updateCartForLoyalty(customerHash, loyaltyCart)
        cartRepository.deleteCartForLoyalty(anonymousCustomerHash)
        val updated = cartRepository.findLoyaltyCartByCustomerHash(customerHash)
        return updated!!
    }
    suspend fun mergeCartForGC(customerHash: String, anonymousCustomerHash: String):  GiftCardCartResponse{
        log.info("$customerHash : merge cart request for $anonymousCustomerHash")
        val giftCardCart = cartRepository.findGiftCardCartByCustomerHash(anonymousCustomerHash)?: throw HttpResponseException("Guest cart is empty", HttpStatusCode.BadRequest)
        log.info("Get Cart Details successful for anonymousCustomer# for GC: + $anonymousCustomerHash")
        cartRepository.updateCartForGC(customerHash, giftCardCart)
        cartRepository.deleteCartForGC(anonymousCustomerHash)
        val updated = cartRepository.findGiftCardCartByCustomerHash(customerHash)
        return DataMapperUtils.mapGiftCardCartResponse(updated!!)
    }

    suspend fun getCartItemCount(customerHash: String): ItemCountResponse {
        log.info("$customerHash : get cart ItemCount request")
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        val itemsCount = cart?.items?.get(0)?.hotel?.get(0)?.room?.size ?: 0
        val category: String = cart!!.items?.get(0)!!.category
        log.info("No. of items in Cart for customer#: $customerHash is $itemsCount")
        return ItemCountResponse(category, itemsCount)
    }

    suspend fun updateCart(customerHash: String, updateCartRequest: CartRequest): Any {
        log.info("$customerHash :updateCart request ${updateCartRequest.json}")
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        return updateItems(cart!!, updateCartRequest)
    }

    suspend fun removeItem(customerHash: String, removeItemRequest: RemoveItemRequest): Any {
        log.info("$customerHash :Remove Item request ${removeItemRequest.json}")
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        removeItemRequest.rooms.forEach{room->
            if (cart == null || cart.items.isNullOrEmpty()) {
                log.error("CART ID: $customerHash with Category: ${room.roomType} Not Found in cart for customer#: $customerHash")
                throw HttpResponseException(CART_ID_WITH_ROOMID_NOT_MATCHED_ERR_MSG, HttpStatusCode.NotFound)
            } else {
                if (!deleteRoom(removeItemRequest.hotelId,customerHash, cart, room)) {
                    log.error("CART ID: $customerHash with Category: ${room.roomType} Not Found in cart for customer#: $customerHash")
                    throw HttpResponseException(CART_ID_WITH_ROOMID_NOT_MATCHED_ERR_MSG, HttpStatusCode.NotFound)
                }
            }
        }
        cartRepository.updateCart(customerHash, cart!!)
        log.info("$customerHash is removed from Cart successful for customer#: $customerHash")
        return if (cart.items?.isEmpty()!!) {
            DataMapperUtils.mapCartEmptyResponse(customerHash)
        } else getPaymentLabels(cart, true)
    }


    suspend fun deleteRoomsForNoAvailability(customerHash: String, removeItemRequest: RemoveItemRequest): Any {
        log.info("$customerHash :Remove Item request ${removeItemRequest.json}")
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        if (cart == null || cart.items.isNullOrEmpty()) {
            throw HttpResponseException(CART_ID_WITH_ROOMID_NOT_MATCHED_ERR_MSG, HttpStatusCode.NotFound)
        }
        removeItemRequest.rooms.forEach{room->
                if (!deleteRoom(removeItemRequest.hotelId,customerHash, cart, room)) {
                    log.error("CART ID: $customerHash with Category: ${room.roomType} Not Found in cart for customer#: $customerHash")
                    throw HttpResponseException(CART_ID_WITH_ROOMID_NOT_MATCHED_ERR_MSG, HttpStatusCode.NotFound)
                }
        }
        cartRepository.updateCart(customerHash, cart)
        val updateRoomNumber = cartRepository.findCartByCustomerHash(customerHash)
         if(updateRoomNumber?.items!!.isNotEmpty()){
            if (!updateRoomNumber.items[0].hotel[0].room.isNullOrEmpty()) {
                updateRoomNumber.items[0].hotel[0].room?.forEachIndexed { index, room ->
                    room.roomNumber = index + 1
                }
            }
            cartRepository.updateCart(customerHash, updateRoomNumber)
            val cartPaymentLabelsRes = getPaymentLabels(updateRoomNumber, true)
            if(removeItemRequest.orderId.isNotEmpty()) {
                val paymentMethod = if(cartPaymentLabelsRes.paymentLabels?.confirmBooking == true || cartPaymentLabelsRes.paymentLabels?.isInternational == true){
                    PAY_AT_HOTEL
                }else{
                    PAY_ONLINE
                }
                updateOrderRoomDetails(cartPaymentLabelsRes, removeItemRequest.orderId, paymentMethod)
            }
            return cartPaymentLabelsRes
        }else{
           return DataMapperUtils.mapCartEmptyResponse(customerHash)
        }

    }

    private fun deleteRoom(hotelId:String,customerHash: String, cart: Cart, removeItem: DeleteRooms): Boolean {
        var price = 0.0
        var tax = 0.0
        var totalPrice = 0.0
        cart.items?.withIndex()?.find { (_, cartItem) ->
            cartItem.hotel.withIndex().find { (_, item) ->
                item.hotelId == hotelId
            }?.let {
                log.info("Cart ID :$customerHash inside ${removeItem.roomId} with Category : ${removeItem.roomNumber} Matched for customer#: $customerHash")
                if (it.value.room!!.size > 1) {
                    it.value.room!!.withIndex().find { (_, r) ->
                        r.roomNumber == (removeItem.roomNumber)
                    }?.let { room ->
                        price = if(cart.items.first().hotel.first().voucherRedemption?.isComplementary == true){
                            0.0
                        }else room.value.cost
                        tax = room.value.tax?.amount!!
                        totalPrice = price + tax
                        cartItem.basePrice -= price
                        cartItem.tax -= tax
                        cartItem.totalPrice -= totalPrice
                        log.info("deposit flag..."+cart.items.first().isDepositAmount+"::"+room.value.roomDepositAmount+"::"+cartItem.payableAmount)
                        if(cart.items.first().isDepositAmount){
                            if(room.value.roomDepositAmount > 0.0){
                                cartItem.payableAmount -= room.value.roomDepositAmount
                            }
                            log.info("payableAmount "+cartItem.payableAmount )
                            if(cartItem.payableAmount <= 0.0){
                                cartItem.payableAmount = cartItem.totalPrice
                                cartItem.totalDepositAmount = 0.0
                                cartItem.balancePayable = 0.0
                            }
                        }else{
                            cartItem.payableAmount -= totalPrice
                        }
                        log.info("payable amount ${cartItem.payableAmount} cart items ${cart.json}")
                        cart.priceSummary = PriceSummary(
                            totalPrice = cartItem.totalPrice,
                            giftCardPrice = cart.priceSummary!!.giftCardPrice,
                            voucher = cart.priceSummary!!.voucher,
                            neuCoins = cart.priceSummary!!.neuCoins,
                            totalPayableAmount = cartItem.payableAmount
                        )
                        cart.paymentDetails?.first()?.txnNetAmount= cartItem.payableAmount
                        it.value.room!!.removeAt(room.index)

                    } ?: return false
                } else {
                    it.value.room!!.find { room ->
                        room.roomNumber == removeItem.roomNumber
                    }?.let { rm ->
                        price = if(cart.items.first().hotel.first().voucherRedemption?.isComplementary == true){
                            0.0
                        }else rm.cost
                        tax = rm.tax?.amount!!
                        totalPrice = price + tax
                        cartItem.basePrice -= price
                        cartItem.totalPrice -= totalPrice
                        cartItem.payableAmount -= totalPrice
                        cart.priceSummary = null
                        cart.paymentDetails = null
                        log.info("cart items ${cart.json}")
                        cart.items.removeAt(it.index)
                    } ?: return false
                }
                it.value.grandTotal = it.value.grandTotal?.minus(totalPrice)
                it.value.totalBasePrice = it.value.totalBasePrice?.minus(price)
                it.value.totalTaxPrice = it.value.totalTaxPrice?.minus(tax)
                return true
            } ?: return false
        }
        return false
    }

    suspend fun emptyCart(customerHash: String): String {
        log.info("$customerHash : removeItems request ${customerHash.json}")
        if (customerHash.isEmpty()) {
            log.info("Items Quantity Mismatch while removing items for customer: $customerHash")
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        } else {
            cartRepository.deleteBookingCart(customerHash)
            log.info("Deleted cart successfully for customer: $customerHash")
        }
        return "SUCCESS"
    }

    suspend fun emptyGCCart(customerHash: String): String {
        log.info("$customerHash : removeItems request ${customerHash.json}")
        val cart = cartRepository.findGiftCardCartByCustomerHash(customerHash)
        if (cart == null) {
            log.info("Items Quantity Mismatch while removing items for customer: $customerHash")
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        } else {
            cartRepository.deleteCartForGC(customerHash)
            log.info("Deleted cart successfully for customer: $customerHash")
        }
        return "SUCCESS"
    }

    suspend fun reviewCart(customerHash: String): Any {
        val cart = cartRepository.findCartByCustomerHash(customerHash)
        val checkAvailabilityURL = prop.hudiniServiceHost.plus(CHECK_AVAILABILITY_URL)
        if ((cart == null) || (cart.items.isNullOrEmpty())) {
            log.info("Cart is empty with this customerHash: $customerHash")
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        } else {
            log.info("cart details based on customerHash ${cart.json}")
            val reviewCartReq = DataMapperUtils.mapCartForCheckAvailability(cart)
            try {
                log.info("calling check availability api")

                val response = client.post(checkAvailabilityURL) {

                    contentType(ContentType.Application.Json)
                    headers {
                        append(CUSTOMERHASH, customerHash)
                    }
                    setBody(reviewCartReq)
                }
                val availabilityResponse = response.body<AvailabilityResponse>()
                log.info("check availability response ${availabilityResponse.json}")

                val checkAvailability = checkAvailability(availabilityResponse, cart)
                log.info("check availability ${checkAvailability.json}")
                return checkAvailability
            } catch (e: Exception) {
                log.info("Exception occurred while calling api ${e.message}")
            }
        }
        return Any()
    }

    private suspend fun checkAvailability(availabilityResponse: AvailabilityResponse, cart: Cart): CartResponse {
        log.info("enter into check availability")
        cart.items?.forEach { cartItems ->
            cartItems.hotel.forEach { hotel ->
                if (hotel.hotelId == availabilityResponse.hotelId) {
                    hotel.room!!.forEach { room ->
                        availabilityResponse.roomTypes.withIndex().find { (_, rt) ->
                            room.roomTypeCode == rt.roomTypeCode
                        }?.let { roomType ->
                            roomType.value.packages.withIndex().find { (_, p) ->
                                p?.packageCode.equals(room.packageCode)
                            }?.let {
                                if (room.cost == it.value?.amount)
                                    room.message = PRIZE_NOT_CHANGED
                                else {
                                    cartItems.basePrice -= room.cost
                                    room.cost = it.value!!.amount
                                    room.message = PRIZE_CHANGED
                                    cartItems.basePrice += it.value!!.amount
                                    cartItems.totalPrice = cartItems.basePrice + cartItems.tax
                                    cartItems.payableAmount = cartItems.totalPrice
                                    cart.priceSummary!!.totalPrice = cartItems.totalPrice
                                    cart.priceSummary!!.totalPayableAmount = cartItems.totalPrice
                                }
                                room.isServiceable = true
                            } ?: run {
                                room.isServiceable = false
                                room.message = ROOM_NOT_AVAILABLE
                            }

                        } ?: run {
                            room.isServiceable = false
                            room.message = ROOM_NOT_AVAILABLE
                        }
                    }
                } else {
                    throw HttpResponseException(CART_ID_WITH_ROOM_NUMBER_NOT_MATCHED_ERR_MSG, HttpStatusCode.NotFound)
                }
            }
        }
        cartRepository.saveCart(cart)
        log.info("mapping cost values ${cart.json}")
        return DataMapperUtils.mapGetCartResponse(cart, null)
    }

    private suspend fun updateItems(cart: Cart, cartRequest: CartRequest): CartResponse {
        var roomCost = 0.0
        var tax = 0.0
        cart.items?.forEach { cartItems ->
            cartItems.hotel.forEach { hotel ->
                cartRequest.hotel.forEach { htl ->
                    if (hotel.hotelId == htl.hotelId) {
                        hotel.room!!.withIndex().find { rm ->
                            rm.value.roomNumber == (htl.room[0].roomNumber)
                        }?.let { r ->
                            roomCost = r.value.cost
                            tax = r.value.tax?.amount!!
                            r.value.cost = 0.0
                            r.value.packageCode = ""
                            r.value.roomId = ""
                            r.value.roomName = ""
                            r.value.roomTypeCode = ""
                            r.value.isServiceable = false
                            r.value.message = ""
                            r.value.rateDescription = ""
                            r.value.roomDescription = ""
                            r.value.rateCode = ""
                            r.value.packageName = ""
                            r.value.roomImgUrl = ""
                            r.value.tax?.amount = 0.0
                            r.value.checkIn = ""
                            r.value.checkOut = ""
                        } ?: throw HttpResponseException(
                            CART_ID_WITH_ROOM_NUMBER_NOT_MATCHED_ERR_MSG,
                            HttpStatusCode.NotFound
                        )
                    }
                }
            }
            val total = tax + roomCost
            cartItems.tax -= tax
            cartItems.basePrice -= roomCost
            cartItems.totalPrice -= total
            cartItems.payableAmount = cartItems.totalPrice
            cart.priceSummary = PriceSummary(
                totalPrice = cartItems.totalPrice,
                giftCardPrice = 0.0,
                neuCoins = 0.0,
                voucher = 0.0,
                totalPayableAmount = cartItems.totalPrice,
            )
            cart.paymentDetails = null
        }
        cartRepository.saveCart(cart)
        return DataMapperUtils.mapGetCartResponse(cart, null)
    }

    suspend fun addTenderModes(customerHash: String, tenderModes: TenderModes): Any? {
        when {
            tenderModes.type.equals(HOTEL_BOOKING, ignoreCase = true) -> {
                val cart = cartRepository.findCartByCustomerHash(customerHash)
                if ((cart == null) || (cart.items.isNullOrEmpty())) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return addInitialTenderMode(cart, tenderModes)
                }
            }
            tenderModes.type.equals(MEMBERSHIP_PURCHASE, ignoreCase = true) -> {
                val cart = cartRepository.findLoyaltyCartByCustomerHash(customerHash)
                if (cart == null) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return addTenderModeForLoyalty(cart, tenderModes)
                }
            }
            tenderModes.type.equals(GIFT_CARD_PURCHASE, ignoreCase = true) -> {
                val cart = cartRepository.fetchGiftCardByCustomerHash(customerHash)
                if (cart == null) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return addInitialTenderModeForGC(cart, tenderModes)
                }
            }
            else -> return null
        }
    }
    private suspend fun addInitialTenderModeForGC(cart: GiftCardCart, tenderModes: TenderModes): Any {
        val payableAmount = cart.priceSummary?.totalPayableAmount
        return if (payableAmount == 0.0) {
            throw HttpResponseException(TOTAL_PAYABLE_AMOUNT_ZERO, HttpStatusCode.ExpectationFailed)
        } else if (tenderModes.tenderMode == TenderMode.TATA_NEU) {
            addNeuCoinsTenderModeForGC(cart, tenderModes, payableAmount!!)
        } else HttpResponseException(PROVIDE_VALID_TENDER_MODE, HttpStatusCode.NotFound)
    }
    private suspend fun addNeuCoinsTenderModeForGC(
        cart: GiftCardCart,
        tenderModes: TenderModes,
        totalPrice: Double
    ): GiftCardCartResponse {
        lateinit var priceSummary: GiftCardPriceSummary
        var totalAmount = totalPrice
        cart.paymentDetails?.withIndex()?.find { (_, tataNeu) ->
            tataNeu.paymentType == tenderModes.tenderMode.toString()
        }?.let {
            throw HttpResponseException("neuCoins already added, if you want to change please remove and add", HttpStatusCode.BadRequest)

        } ?: run {
            if (tenderModes.tenderModeDetails[0].amount <= totalPrice) {
                val neucoins = tenderModes.tenderModeDetails[0].amount
                totalAmount -= neucoins
                log.info("Total price after neu coin redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,tenderModes.tenderModeDetails[0].amount)
                priceSummary = DataMapperUtils.mapNeuCoinsGCPriceSummary(totalAmount, cart, neucoins)
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            } else {
                val totalPayableAmount = totalAmount
                totalAmount -= totalAmount
                log.info("Total price after neu coins redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,totalPayableAmount)
                priceSummary = DataMapperUtils.mapNeuCoinsGCPriceSummary(totalAmount, cart, totalPayableAmount)
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            }
        }
        cart.paymentDetails?.withIndex()?.find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }.let {
            it!!.value.txnNetAmount = cart.priceSummary?.totalPayableAmount
            if(cart.priceSummary?.totalPayableAmount == 0.0){
                cart.paymentDetails?.removeAt(it.index)
            }
        }
        cartRepository.saveGiftCardCart(cart)
        if(!tenderModes.orderId.isNullOrEmpty() && !(updateOrder(GIFT_CARD_PURCHASE,UpdateOrderRequest(tenderModes.orderId, cart.paymentDetails, cart.priceSummary?.totalPayableAmount, null, PAY_ONLINE, false)))) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        val latestCart = cartRepository.findGiftCardCartByCustomerHash(cart._id)
        return DataMapperUtils.mapGiftCardCartResponse(latestCart!!)
    }

    private suspend fun addInitialTenderMode(cart: Cart, tenderModes: TenderModes): Any {
        val payableAmount = if (cart.items?.get(0)?.modifiedPaymentDetails != null) {
            cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount!!
        } else cart.items?.get(0)!!.payableAmount
        if (payableAmount == 0.0) {
            throw HttpResponseException(TOTAL_PAYABLE_AMOUNT_ZERO, HttpStatusCode.ExpectationFailed)
        } else return when {
            cart.paymentDetails!!.first().paymentType == CC_AVENUE -> {
                throw HttpResponseException(ADD_TENDER_MODE_ERR_MSG, HttpStatusCode.BadRequest)
            }
            tenderModes.tenderMode == TenderMode.GIFT_CARD -> {
                addGiftCardTenderMode(cart, tenderModes, payableAmount)
            }
            tenderModes.tenderMode == TenderMode.TATA_NEU -> {
                addNeuCoinsTenderMode(cart, tenderModes, payableAmount)
            }
            else -> HttpResponseException(PROVIDE_VALID_TENDER_MODE, HttpStatusCode.NotFound)
        }

    }

    private suspend fun addTenderModeForLoyalty(cart: LoyaltyCart, tenderModes: TenderModes): Any {
        val payableAmount =  cart.priceSummary.totalPayableAmount
        return if (payableAmount == 0.0) {
            throw HttpResponseException(TOTAL_PAYABLE_AMOUNT_ZERO, HttpStatusCode.ExpectationFailed)
        }else if (tenderModes.tenderMode == TenderMode.TATA_NEU) {
            addNeuCoinsTenderModeForLoyalty(cart, tenderModes, payableAmount)
        } else HttpResponseException(PROVIDE_VALID_TENDER_MODE, HttpStatusCode.NotFound)

    }

    private suspend fun addNeuCoinsTenderModeForLoyalty(
        cart: LoyaltyCart,
        tenderModes: TenderModes,
        totalPrice: Double
    ): LoyaltyCart {
        lateinit var priceSummary: LoyaltyPriceSummary
        var totalAmount = totalPrice
        cart.paymentDetails.withIndex().find { (_, tataNeu) ->
            tataNeu.paymentType == tenderModes.tenderMode.toString()
        }?.let {
            throw HttpResponseException("neuCoins already added, if you want to change please remove and add", HttpStatusCode.BadRequest)

        } ?: run {
            if (tenderModes.tenderModeDetails[0].amount <= totalPrice) {
                val neucoins = tenderModes.tenderModeDetails[0].amount
                totalAmount -= neucoins
                log.info("Total price after neu coin redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,tenderModes.tenderModeDetails[0].amount)
                priceSummary = DataMapperUtils.mapNeuCoinsLoyaltyPriceSummary(totalAmount, cart, neucoins)
                cart.priceSummary.totalPayableAmount = totalAmount
                cart.priceSummary = priceSummary
                cart.paymentDetails.addAll(paymentDetails)
            } else {
                val totalPayableAmount = totalAmount
                totalAmount -= totalAmount
                log.info("Total price after neu coins redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,totalPayableAmount)
                priceSummary = DataMapperUtils.mapNeuCoinsLoyaltyPriceSummary(totalAmount, cart, totalPayableAmount)
                cart.priceSummary.totalPayableAmount = totalAmount
                cart.priceSummary = priceSummary
                cart.paymentDetails.addAll(paymentDetails)
            }
        }
        cart.paymentDetails.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }.let {
            it!!.value.txnNetAmount = cart.priceSummary.totalPayableAmount
            if(cart.priceSummary.totalPayableAmount == 0.0){
                cart.paymentDetails.removeAt(it.index)
            }
        }
        cartRepository.saveLoyaltyCart(cart)
        if(!tenderModes.orderId.isNullOrEmpty() && !(updateOrder(MEMBERSHIP_PURCHASE,UpdateOrderRequest(tenderModes.orderId, cart.paymentDetails, cart.priceSummary.totalPayableAmount, null, PAY_ONLINE, false)))){
                throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
            }
        return cart
    }


    private suspend fun addNeuCoinsTenderMode(
        cart: Cart,
        tenderModes: TenderModes,
        totalPrice: Double
    ): CartResponse {
        lateinit var priceSummary: PriceSummary
        var totalAmount = totalPrice
        cart.paymentDetails?.withIndex()?.find { (_, tataNeu) ->
            tataNeu.paymentType == tenderModes.tenderMode.toString()
        }?.let {
            val errorMessage = ErrorMessage(BAD_REQUEST_STATUS_CODE,"${tenderModes.tenderModeDetails[0].cardNumber} $SAME_GIFT_CARD_ERROR_MSG")
            return DataMapperUtils.mapGetCartResponse(cart, errorMessage)

        } ?: run {
            if (tenderModes.tenderModeDetails[0].amount <= totalPrice) {
                val neucoins = tenderModes.tenderModeDetails[0].amount
                totalAmount -= neucoins
                log.info("Total price after neu coin redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,tenderModes.tenderModeDetails[0].amount)
                if(cart.items!![0].modifiedPaymentDetails!=null){
                    priceSummary = DataMapperUtils.mapModifiedNeuCoinsPriceSummary(totalAmount, cart, neucoins)
                    cart.items.first().modifiedPaymentDetails!!.modifiedPayableAmount = totalAmount
                }else{
                    priceSummary = DataMapperUtils.mapNeuCoinsPriceSummary(totalAmount, cart, neucoins)
                    cart.items.first().payableAmount = totalAmount
                }
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            } else {
                val totalPayableAmount = totalAmount
                totalAmount -= totalAmount
                log.info("Total price after neu coins redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapNeuCoinsPaymentDetails(tenderModes,totalPayableAmount)
                if(cart.items!![0].modifiedPaymentDetails!=null){
                    priceSummary = DataMapperUtils.mapModifiedNeuCoinsPriceSummary(totalAmount, cart, totalPayableAmount)
                    cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount = totalAmount
                } else {
                    priceSummary = DataMapperUtils.mapNeuCoinsPriceSummary(totalAmount, cart, totalPayableAmount)
                    cart.items[0].payableAmount = totalAmount
                }
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            }
        }
        cart.paymentDetails!!.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }.let {
            it!!.value.txnNetAmount = cart.items!!.first().payableAmount
            if(cart.items.first().payableAmount == 0.0){
                cart.paymentDetails!!.removeAt(it.index)
            }
        }
        cartRepository.saveCart(cart)
        if(!tenderModes.orderId.isNullOrEmpty() && !(updateOrder(HOTEL_BOOKING, UpdateOrderRequest(tenderModes.orderId, cart.paymentDetails, cart.items?.first()?.payableAmount, cart.items?.first()?.balancePayable, PAY_ONLINE, cart.items!!.first().isDepositAmount)))) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        return DataMapperUtils.mapGetCartResponse(cart, null)
    }
    private suspend fun addGiftCardTenderMode(
        cart: Cart,
        tenderModes: TenderModes,
        totalPrice: Double
    ): CartResponse {
        lateinit var priceSummary: PriceSummary
        var totalAmount = totalPrice
        cart.paymentDetails?.withIndex()?.find { (_, giftCardNumber) ->
            giftCardNumber.cardNumber == tenderModes.tenderModeDetails[0].cardNumber
        }?.let {

            val errorMessage = ErrorMessage(BAD_REQUEST_STATUS_CODE,"${tenderModes.tenderModeDetails[0].cardNumber} $SAME_GIFT_CARD_ERROR_MSG")
            return DataMapperUtils.mapGetCartResponse(cart, errorMessage)
        } ?: run {
            if (tenderModes.tenderModeDetails[0].amount <= totalPrice) {
                val giftCardAmount = tenderModes.tenderModeDetails[0].amount
                totalAmount -= giftCardAmount
                log.info("Total price after gift card redemption $totalAmount")
                val paymentDetails =
                    DataMapperUtils.mapGiftCardPaymentDetails(tenderModes, tenderModes.tenderModeDetails[0].amount)
                if (cart.items!![0].modifiedPaymentDetails != null) {
                    priceSummary = DataMapperUtils.mapModifiedGiftCardsPriceSummary(totalAmount, cart, giftCardAmount)
                    cart.items.first().modifiedPaymentDetails!!.modifiedPayableAmount = totalAmount
                } else {
                    priceSummary = DataMapperUtils.mapGiftCardPriceSummary(totalAmount, cart, giftCardAmount)
                    cart.items.first().payableAmount = totalAmount
                }
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            } else {
                val totalPayableAmount = totalAmount
                totalAmount -= totalAmount
                log.info("Total price after gift card redemption $totalAmount")
                val paymentDetails = DataMapperUtils.mapGiftCardPaymentDetails(tenderModes, totalPayableAmount)

                if (cart.items!![0].modifiedPaymentDetails != null) {
                    priceSummary =
                        DataMapperUtils.mapModifiedGiftCardsPriceSummary(totalAmount, cart, totalPayableAmount)
                    cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount = totalAmount
                } else {
                    priceSummary = DataMapperUtils.mapGiftCardPriceSummary(totalAmount, cart, totalPayableAmount)
                    cart.items[0].payableAmount = totalAmount
                }
                cart.priceSummary = priceSummary
                cart.paymentDetails?.addAll(paymentDetails)
            }
        }
        cart.paymentDetails!!.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }?.let {
            it.value.txnNetAmount = cart.items!!.first().payableAmount
            if(cart.items.first().payableAmount == 0.0){
                cart.paymentDetails!!.removeAt(it.index)
            }
        }
        cartRepository.saveCart(cart)
        if(!tenderModes.orderId.isNullOrEmpty() && !(updateOrder(HOTEL_BOOKING, UpdateOrderRequest(tenderModes.orderId, cart.paymentDetails, cart.items?.first()?.payableAmount, cart.items?.first()?.balancePayable, PAY_ONLINE, cart.items!!.first().isDepositAmount)))) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        return DataMapperUtils.mapGetCartResponse(cart, null)
    }

    suspend fun deleteTenderMode(customerHash: String, deleteTenderMode: DeleteTenderMode): Any? {
        when {
            deleteTenderMode.type.equals(HOTEL_BOOKING, ignoreCase = true) -> {
                val cart = cartRepository.findCartByCustomerHash(customerHash)
                if ((cart == null) || (cart.items.isNullOrEmpty()) || (cart.paymentDetails.isNullOrEmpty())) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return deleteTender(deleteTenderMode, cart)
                }
            }
            deleteTenderMode.type.equals(MEMBERSHIP_PURCHASE, ignoreCase = true) -> {
                val cart = cartRepository.findLoyaltyCartByCustomerHash(customerHash)
                if (cart == null) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return deleteTenderForLoyalty(deleteTenderMode, cart)
                }
            }
            deleteTenderMode.type.equals(GIFT_CARD_PURCHASE, ignoreCase = true) -> {
                val cart = cartRepository.fetchGiftCardByCustomerHash(customerHash)
                if (cart == null) {
                    log.info("Cart is empty with this customerHash: $customerHash")
                    throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
                } else {
                    return  deleteTender(deleteTenderMode,cart)
                }
            }
            else -> return null
        }
    }
    private suspend fun deleteTender(deleteTenderMode: DeleteTenderMode,cart: GiftCardCart):GiftCardCartResponse{
        if(cart.priceSummary?.neuCoins!! <= 0.0){
            return DataMapperUtils.mapGiftCardCartResponse(cart)
        }
        if (deleteTenderMode.tenderMode.toString()== TATA_NEU){
            cart.priceSummary = GiftCardPriceSummary(
                cart.priceSummary?.totalPrice,
                cart.priceSummary?.neuCoins?.plus(-(deleteTenderMode.amount)),cart.priceSummary?.totalPayableAmount?.plus(deleteTenderMode.amount)
            )
            cart.paymentDetails!!.withIndex().find { (_, paymentDetails) ->
                paymentDetails.paymentType == deleteTenderMode.tenderMode.toString()
            }?.let { payment ->
                cart.paymentDetails!!.removeAt(payment.index)
            }
        }
        cart.paymentDetails!!.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }?.let {
            it.value.txnNetAmount = cart.priceSummary?.totalPrice
            it.value.txnStatus = INITIATED
        }?:run {
            if(cart.priceSummary?.totalPrice!=0.0) {
                cart.paymentDetails!!.add(DataMapperUtils.mapJusPayPaymentDetailsOfGC(cart.priceSummary?.totalPrice))
            }
        }
        cartRepository.saveGiftCardCart(cart)
        if(!deleteTenderMode.orderId.isNullOrEmpty() && !updateOrder(deleteTenderMode.type,
                updateOrderRequest = UpdateOrderRequest(deleteTenderMode.orderId,cart.paymentDetails,cart.priceSummary?.totalPayableAmount,null,null,false) )) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        return DataMapperUtils.mapGiftCardCartResponse(cart)
    }

    private suspend fun deleteTenderForLoyalty(deleteTenderMode: DeleteTenderMode, cart: LoyaltyCart): LoyaltyCart {
        if(cart.priceSummary.neuCoins <= 0.0){
            return cart
        }
        if (deleteTenderMode.tenderMode == TenderMode.TATA_NEU) {
            log.info("you have decided to remove the neuCoins!!")
                cart.priceSummary.totalPayableAmount += deleteTenderMode.amount
                cart.priceSummary = LoyaltyPriceSummary(
                    price = cart.priceSummary.price,
                    tax = cart.priceSummary.tax,
                    totalPayableAmount = cart.priceSummary.totalPayableAmount,
                    neuCoins = cart.priceSummary.neuCoins.plus(-(deleteTenderMode.amount)),
                    totalPrice = cart.priceSummary.totalPrice,
                    discountTax = cart.priceSummary.discountTax,
                    discountPrice = cart.priceSummary.discountPrice,
                    discountPercent = cart.priceSummary.discountPercent
                )

            cart.paymentDetails.withIndex().find { (_, paymentDetails) ->
                paymentDetails.paymentType == deleteTenderMode.tenderMode.toString()
            }?.let { payment ->
                cart.paymentDetails.removeAt(payment.index)
            }

        }
        cart.paymentDetails.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }?.let {
            it.value.txnNetAmount = cart.priceSummary.totalPayableAmount
            it.value.txnStatus = INITIATED
        }?:run {
            if(cart.priceSummary.totalPayableAmount!=0.0) {
                cart.paymentDetails.addAll(DataMapperUtils.mapLoyaltyPaymentDetails(cart.priceSummary.totalPayableAmount))
            }
        }
        cartRepository.saveLoyaltyCart(cart)
        if(!deleteTenderMode.orderId.isNullOrEmpty() && !(updateOrder(MEMBERSHIP_PURCHASE, UpdateOrderRequest(deleteTenderMode.orderId, cart.paymentDetails, cart.priceSummary.totalPayableAmount, null, PAY_ONLINE, false)))) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        return cart
    }

    private suspend fun deleteTender(deleteTenderMode: DeleteTenderMode, cart: Cart): Any {
        if (deleteTenderMode.tenderMode == TenderMode.GIFT_CARD) {
            if (cart.items!![0].modifiedPaymentDetails != null) {
                cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount =
                    cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount?.plus(
                        deleteTenderMode.amount
                    )
                cart.priceSummary = PriceSummary(
                    totalPayableAmount = cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount!!,
                    giftCardPrice = cart.priceSummary?.giftCardPrice!!.plus(-(deleteTenderMode.amount)),
                    neuCoins = cart.priceSummary?.neuCoins!!,
                    voucher = cart.priceSummary?.voucher!!,
                    totalPrice = cart.items[0].modifiedPaymentDetails!!.modifiedTotalPrice
                )
            } else {
                cart.items[0].payableAmount += deleteTenderMode.amount
                cart.priceSummary = PriceSummary(
                    totalPayableAmount = cart.items[0].payableAmount,
                    giftCardPrice = cart.priceSummary?.giftCardPrice!!.plus(-(deleteTenderMode.amount)),
                    neuCoins = cart.priceSummary?.neuCoins!!,
                    voucher = cart.priceSummary?.voucher!!,
                    totalPrice = cart.priceSummary?.totalPrice
                )
            }
            cart.paymentDetails!!.withIndex().find { (_, paymentDetails) ->
                paymentDetails.cardNumber == encrypt(deleteTenderMode.cardNumber)
            }?.let { payment ->
                cart.paymentDetails!!.removeAt(payment.index)
            }?: run {
                throw HttpResponseException(deleteTenderMode.cardNumber.plus(CARD_NUMBER_NOT_FOUND), HttpStatusCode.NotFound)
            }
        }
        else if (deleteTenderMode.tenderMode == TenderMode.TATA_NEU) {
            if(cart.priceSummary?.neuCoins!! <= 0.0){
                DataMapperUtils.mapGetCartResponse(cart, null)
            }
            log.info("you have decided to remove the neuCoins!!")
            if (cart.items!![0].modifiedPaymentDetails != null) {
                cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount =
                    cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount?.plus(
                        deleteTenderMode.amount
                    )
                cart.priceSummary = PriceSummary(
                    totalPayableAmount = cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount!!,
                    giftCardPrice = cart.priceSummary?.giftCardPrice!!,
                    neuCoins = cart.priceSummary?.neuCoins!!.plus(-(deleteTenderMode.amount)),
                    voucher = cart.priceSummary?.voucher!!,
                    totalPrice = cart.items[0].modifiedPaymentDetails!!.modifiedTotalPrice
                )
            } else {
                cart.items[0].payableAmount += deleteTenderMode.amount
                cart.priceSummary = PriceSummary(
                    totalPayableAmount = cart.items[0].payableAmount,
                    giftCardPrice = cart.priceSummary?.giftCardPrice!!,
                    neuCoins = cart.priceSummary?.neuCoins!!.plus(-(deleteTenderMode.amount)),
                    voucher = cart.priceSummary?.voucher!!,
                    totalPrice = cart.priceSummary?.totalPrice
                )
            }
            cart.paymentDetails!!.withIndex().find { (_, paymentDetails) ->
                paymentDetails.paymentType == deleteTenderMode.tenderMode.toString()
            }?.let { payment ->
                cart.paymentDetails!!.removeAt(payment.index)
            }

        }
        cart.paymentDetails!!.withIndex().find {(_,payment) ->
            payment.paymentType == JUS_PAY
        }?.let {
            it.value.txnNetAmount = cart.items!!.first().payableAmount
            it.value.txnStatus = INITIATED
        }?:run {
            if(cart.items!!.first().payableAmount!=0.0) {
                cart.paymentDetails!!.addAll(DataMapperUtils.mapPaymentDetails(cart))
            }
        }
        cartRepository.saveCart(cart)
        if(!deleteTenderMode.orderId.isNullOrEmpty() && !(updateOrder(HOTEL_BOOKING, UpdateOrderRequest(deleteTenderMode.orderId, cart.paymentDetails, cart.items?.first()?.payableAmount, cart.items?.first()?.balancePayable, PAY_ONLINE, cart.items!!.first().isDepositAmount)))) {
            throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
        }
        return DataMapperUtils.mapGetCartResponse(cart, null)
    }


   /* suspend fun getCartByOrderId(modifyBooking: ModifyBooking): CartResponse {
        val roomCost = arrayListOf<Double>()
        val taxAmount = arrayListOf<Double>()
        val taxObject = arrayListOf<Tax>()
        val bookingPolicyDescription = arrayListOf<String?>()
        val cancelPolicyDescription = arrayListOf<String?>()
        val description = arrayListOf<String?>()
        val detailedDescription = arrayListOf<String?>()
        val paymentLabels = arrayListOf<String?>()
        var dailyRates: List<DailyRates>?
        val list = ArrayList<List<DailyRates>?>()
        val orderURL = prop.orderServiceHost.plus(FETCH_ORDER_URL)
        val response = client.get("$orderURL?OrderId=${modifyBooking.orderId}&emailId=${modifyBooking.emailId}") {
            contentType(ContentType.Application.Json)
        }
        log.info("modify order response ${response.bodyAsText()}")
        if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
            val order = response.body() as Order
            var cart = cartRepository.findCartByCustomerHash(order.customerHash)
            log.info("modified request ${modifyBooking.json}")
            log.info("modify booking order ${order.json}")
            if (order.orderStatus == SUCCESS || order.orderStatus == PARTIALLY_CANCELLED) {
                val paymentMethod = order.paymentMethod
                val hotelId = order.orderLineItems.first().hotel?.hotelId
                val roomSize = modifyBooking.modifyBookingDetails.size
                for (i in 0 until roomSize) {
                    val rateCodeResponse = modifyBookingRateFiltersRateCode(hotelId.toString(), modifyBooking, i)
                    log.info("Rate code response ${rateCodeResponse.json}")
                    rateCodeResponse.roomTypes!!.forEach { roomType ->
                        if (roomType.roomCode == modifyBooking.modifyBookingDetails[i].roomTypeCode) {
                            roomType.rooms?.forEach { roomInfo ->
                                if (roomInfo.rateCode == modifyBooking.modifyBookingDetails[i].rateCode) {
                                    roomCost.add(roomInfo.total?.amount!!)
                                    taxAmount.add(roomInfo.tax?.amount!!)
                                    taxObject.add(roomInfo.tax)
                                    log.info("room and tax , $roomCost, $taxAmount")

                                    description.add(roomInfo.rateContent?.details?.description)
                                    detailedDescription.add(roomInfo.rateContent?.details?.detailedDescription)

                                    cancelPolicyDescription.add(roomInfo.cancellationPolicy?.description)
                                    bookingPolicyDescription.add(roomInfo.bookingPolicy?.description)
                                    paymentLabels.add(roomInfo.bookingPolicy?.code)
                                    dailyRates= roomInfo.daily?.map {
                                        DailyRates(
                                            date = it.date,
                                            amount = it.price?.amount,
                                            tax = TaxBreakDown(
                                                amount = it.price?.tax?.amount,
                                                breakDown = it.price?.tax?.breakDown?.map { breakdown->
                                                    val name=rateCodeResponse.chargeList?.find { it.code==breakdown.code }?.name
                                                    BreakDownDetails(
                                                        amount = breakdown.amount,
                                                        code = breakdown.code,
                                                        name=name
                                                    )
                                                }
                                            )
                                        )
                                    }
                                    list.add(dailyRates)
                                }
                            }
                        }
                    }
                }
                log.info("list----${list}")
                if (roomCost.size != roomSize && taxAmount.size != roomSize) {
                    throw HttpResponseException(RATE_CODE_NOT_FOUND, HttpStatusCode.NotFound)
                }
                if (cart == null || cart.items?.isEmpty() == true) {
                    cart = DataMapperUtils.mapCartDetails(order, modifyBooking)
                    log.info("modified cart details ${cart.json}")
                }
                cart = DataMapperUtils.mapModifiedRoom(
                    cart,
                    modifyBooking,
                    roomCost,
                    taxAmount,
                    taxObject,
                    description,
                    detailedDescription,
                    cancelPolicyDescription,
                    bookingPolicyDescription,
                    paymentLabels,
                    list
                )
                log.info("cart${cart.json}")
                val complementaryBasePrice =
                    if (order.orderLineItems.first().hotel?.voucherRedemption?.isComplementary == true) {
                        0.0
                    } else {
                        roomCost.sum()
                    }
                cart.items!![0].modifiedPaymentDetails?.modifiedBasePrice = complementaryBasePrice

                cart.items!![0].modifiedPaymentDetails?.modifiedTax = taxAmount.sum()

                cart.items!![0].modifiedPaymentDetails?.modifiedTotalPrice = complementaryBasePrice + taxAmount.sum()

                cart.items!![0].modifiedPaymentDetails?.modifiedPayableAmount = complementaryBasePrice + taxAmount.sum()
                cart.priceSummary = PriceSummary(
                    totalPrice = cart.items!![0].modifiedPaymentDetails?.modifiedTotalPrice!!,
                    giftCardPrice = cart.priceSummary!!.giftCardPrice,
                    neuCoins = cart.priceSummary!!.neuCoins,
                    voucher = cart.priceSummary!!.voucher,
                    totalPayableAmount = cart.items!![0].modifiedPaymentDetails?.modifiedPayableAmount!!
                )
                log.info("price summary ${cart.priceSummary!!.json}")
                if (order.orderLineItems.first().hotel?.voucherRedemption?.isComplementary == true) {
                    cart.items!!.first().basePrice = 0.0
                } else {
                    cart.items!!.first().basePrice =
                        order.orderLineItems.first().hotel?.totalBasePrice.toString().toDouble()
                }
                cart.items!![0].newTotalPrice = cart.items!![0].newTotalPrice!!

                cart.items!!.first().tax = order.orderLineItems.first().hotel?.totalTaxPrice!!
                cart.items!!.first().totalPrice = order.orderLineItems.first().hotel?.grandTotal!!


                cart.items!!.first().newTotalPrice = cart.items!!.first().newTotalPrice?.plus(order.gradTotal)
                var totalPrice = 0.0
                var totalTax = 0.0
                cart.items!!.first().hotel.first().room?.forEach {
                    if (order.orderLineItems.first().hotel?.voucherRedemption?.isComplementary == true) {
                        if (it.modifyBooking != null && it.status == CONFIRMED) {
                            totalTax += it.modifyBooking!!.tax?.amount!!
                        } else if(it.status == CONFIRMED) {
                            totalTax += it.tax?.amount!!
                        }
                    } else {
                        if (it.modifyBooking != null && it.status == CONFIRMED) {
                            totalPrice += it.modifyBooking!!.cost
                            totalTax += it.modifyBooking!!.tax?.amount!!
                        } else if(it.status == CONFIRMED) {
                            totalPrice += it.cost
                            totalTax += it.tax?.amount!!
                        }
                    }

                }
                cart.items!!.first().hotel.first().grandTotal = totalPrice + totalTax
                cart.items!!.first().hotel.first().totalBasePrice = totalPrice
                cart.items!!.first().hotel.first().totalTaxPrice = totalTax

                cart.items!!.first().hotel.first().revisedPrice = totalPrice + totalTax


                var paidAmount = 0.0
                var cancelPayableAmount = 0.0
                if (order.orderStatus == PARTIALLY_CANCELLED) {
                    order.orderLineItems.first().hotel?.rooms?.forEach {
                        if (it.status == "Cancelled") {
                            if (it.cancelPayableAmount != 0.0) {
                                cancelPayableAmount += it.cancelPayableAmount!!
                            } else {
                                paidAmount += it.cancelRefundableAmount
                            }
                        } else {
                            paidAmount += it.paidAmount!!
                        }
                    }
                } else {
                    paidAmount = cart.items!!.first().hotel.first().amountPaid!!
                }


                val modifiedPayableAmount = if (order.paymentMethod == PAY_ONLINE) {
                    (cart.items!!.first().hotel.first().grandTotal!! + cancelPayableAmount) - paidAmount
                } else {
                    cart.items!!.first().hotel.first().grandTotal!!
                }
                log.info("modifiedPayableAmount $modifiedPayableAmount")
                if (modifiedPayableAmount > 0.0) {
                    cart.items!!.first().modifiedPayableAmount = modifiedPayableAmount
                    cart.items!!.first().balancePayable = modifiedPayableAmount
                } else if (modifiedPayableAmount < 0.0) {
                    cart.items!!.first().refundAmount = abs(modifiedPayableAmount)
                    cart.items!!.first().balancePayable = modifiedPayableAmount
                } else {
                    cart.items!!.first().modifiedPayableAmount = modifiedPayableAmount
                    cart.items!!.first().refundAmount = modifiedPayableAmount
                    cart.items!!.first().balancePayable = modifiedPayableAmount
                }
                cart.paymentMethod = paymentMethod
                cartRepository.saveCart(cart)
                log.info("updated the cart for modify booking ${cart.json}")
                return DataMapperUtils.mapGetCartResponse(cart, null)
            } else throw HttpResponseException(ORDER_NOT_YET_PLACED_ERR_MSG, HttpStatusCode.NotFound)

        } else throw HttpResponseException(ORDER_NOT_FOUND_ERR_MSG, response.status)

    }*/

    private suspend fun updateOrder(type:String?,updateOrderRequest: UpdateOrderRequest):Boolean{
        val updateBookingURL = prop.orderServiceHost.plus(UPDATE_ORDER_URL)
        val response = client.post(updateBookingURL) {
            log.info("update booking API Request ${updateOrderRequest.json}")
            headers {
                append(HttpHeaders.ContentType, "application/json")
                append(TYPE,type.toString())

            }
            setBody(updateOrderRequest)
        }
        return response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted || response.status == HttpStatusCode.Created
    }
    private suspend fun updateOrderRoomDetails(cartResponse: CartPaymentLabelResponse?,orderId:String, paymentMethod:String):Boolean{
        val updateOrderBookingUrl = prop.orderServiceHost + UPDATE_ORDER_BOOKING
        val response = client.post(updateOrderBookingUrl) {
            log.info("update order no available rooms ${cartResponse?.json}")
            headers {
                append(HttpHeaders.ContentType, "application/json")
                append(ORDERID,orderId)
                append(PAYMENTMETHOD,paymentMethod)
            }
            setBody(cartResponse)
        }
        log.info("Update Order Room Details Response Received as ${response.bodyAsText()}")
        return response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted || response.status == HttpStatusCode.Created
    }

    suspend fun updatePaymentType(paymentTypeReq: PaymentTypeReq):CartResponse{
        log.info("Payment Type request ${paymentTypeReq.json}")
        ValidatorUtils.validateRequestBody(validatePaymentTypeReq.validate(paymentTypeReq))
        var paymentObject : PaymentDetails? = null
        val cart = cartRepository.findCartByCustomerHash(paymentTypeReq.customerHash)
            ?: throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)

        if (cart.items!!.isEmpty()) {
            throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.NotFound)
        }
        if(cart.items.first().hotel.first().country != COUNTRY_CODE){
            ValidatorUtils.validateRequestBody(validatePaymentDetails.validate(paymentTypeReq))
        }
            cart.paymentDetails!!.forEach {paymentDetails ->
                when {
                    paymentTypeReq.paymentType == PAY_DEPOSIT || paymentTypeReq.paymentType == PAY_FULL || paymentTypeReq.paymentType == PAY_ONLINE || paymentTypeReq.paymentType == PAY_NOW -> {
                        cart.paymentDetails!!.withIndex().find {(_,payment) ->
                            payment.paymentType == JUS_PAY || payment.paymentType == CC_AVENUE ||
                                    payment.paymentType == TATA_NEU || payment.paymentType == GIFT_CARD
                        }?.let {p ->
                            paymentObject = cart.paymentDetails!![p.index]
                        }
                        cart.paymentDetails!!.clear()
                        cart.paymentDetails!!.addAll(listOf( paymentObject!!))
                        cart.paymentDetails!!.first().paymentType = JUS_PAY
                        cart.paymentDetails!!.first().txnStatus = INITIATED
                        cart.paymentDetails!!.first().paymentMethod = ""
                        cart.paymentDetails!!.first().paymentMethodType = ""

                    }
                    else -> {
                        cart.paymentDetails!!.withIndex().find {(_,payment) ->
                            (payment.paymentType == JUS_PAY || payment.paymentType == TATA_NEU ||
                                    payment.paymentType == GIFT_CARD || payment.paymentType == CC_AVENUE) &&
                                    (paymentTypeReq.paymentType == PAY_AT_HOTEL || paymentTypeReq.paymentType == CONFIRM_BOOKING)
                        }?.let {p ->
                            paymentObject = cart.paymentDetails!![p.index]
                        }
                        cart.paymentDetails!!.clear()
                        cart.paymentDetails!!.addAll(listOf( paymentObject!!))
                        cart.paymentDetails!!.first().paymentType = CC_AVENUE
                        cart.paymentDetails!!.first().txnStatus = PENDING
                        cart.paymentDetails!!.first().paymentMethod = ""
                        cart.paymentDetails!!.first().paymentMethodType = ""
                    }
                }
            }
             if(paymentTypeReq.paymentType == PAY_DEPOSIT){
                cart.paymentDetails!!.first().txnNetAmount = cart.items.first().totalDepositAmount
                cart.items.first().payableAmount = cart.items.first().totalDepositAmount
                cart.items.first().isDepositAmount = true
                cart.priceSummary!!.giftCardPrice= 0.0
                cart.priceSummary!!.neuCoins= 0.0
                cart.priceSummary!!.totalPayableAmount = cart.items.first().totalDepositAmount
                cart.items.first().balancePayable =if(cart.items.first().hotel.first().promoType.equals(COUPON_PROMO_TYPE, ignoreCase = true)){
                    cart.items.first().hotel.first().grandTotal!! - cart.items.first().totalDepositAmount
                }else{cart.items.first().totalPrice - cart.items.first().totalDepositAmount}
            }else {
                 cart.items.first().payableAmount = cart.items.first().hotel.first().grandTotal!!
                 cart.priceSummary!!.totalPayableAmount = cart.items.first().hotel.first().grandTotal!!
                 cart.paymentDetails!!.first().txnNetAmount = cart.items.first().hotel.first().grandTotal
                 cart.priceSummary!!.giftCardPrice = 0.0
                 cart.priceSummary!!.neuCoins = 0.0
                 cart.paymentDetails?.first()?.cardNo = paymentTypeReq.cardNo
                 cart.paymentDetails?.first()?.nameOnCard = paymentTypeReq.nameOnCard
                 cart.paymentDetails?.first()?.expiryDate = paymentTypeReq.expiryDate
                 cart.paymentDetails?.first()?.paymentMethod = paymentTypeReq.cardCode
                 cart.items.first().balancePayable = 0.0
            }

            val paymentMethod = when (paymentTypeReq.paymentType) {
                PAY_DEPOSIT, PAY_FULL, PAY_ONLINE, PAY_NOW -> {
                    PAY_ONLINE
                }
                CONFIRM_BOOKING -> {
                    CONFIRM_BOOKING
                }
                else -> {
                    PAY_AT_HOTEL
                }
            }
            cartRepository.saveCart(cart)
            if(!paymentTypeReq.orderId.isNullOrEmpty() && !(updateOrder(HOTEL_BOOKING, UpdateOrderRequest(paymentTypeReq.orderId, cart.paymentDetails, cart.items.first().payableAmount, cart.items.first().balancePayable, paymentMethod, cart.items.first().isDepositAmount )))) {
                throw HttpResponseException(UPDATE_ORDER_ERR_MSG, HttpStatusCode.NotFound)
            }

        return DataMapperUtils.mapGetCartResponse(cart, null)
    }

    private suspend fun getPaymentLabels(cart:Cart, isLabelChange: Boolean): CartPaymentLabelResponse {
        val paymentLabelsList: ArrayList<String> = arrayListOf()
        val cancelPolicyList: ArrayList<String> = arrayListOf()
        val paymentLabelsInfo = PaymentLabels()
        val percentages: Array<Int>?
        var depositAmount = 0.0
        var percentageFlag: Boolean = false
        var depositLabel: String? = null
        var cancelPolicyDescription: String? = null
        val configuredPercentages: String = Constants.percentages
        percentages = configuredPercentages.split(",").map { it.toInt() }.toTypedArray()
        val countryCode= cart.items?.first()?.hotel?.first()?.country

            cart.items?.forEach { cartItems ->
                cartItems.hotel.forEach { hotel ->
                    for (room in hotel.room!!) {
                        room.roomCode?.let { paymentLabelsList.add(it) }
                        room.cancelPolicyCode?.let { cancelPolicyList.add(it) }
                        cancelPolicyDescription = room.cancelPolicyDescription
                    }
                }
            }

            if(cancelPolicyList.isNotEmpty()){
                val cancelPolicy = cancelPolicyList[0]
                if(!cancelPolicyList.all { it == cancelPolicy }){
                    cancelPolicyDescription =""
                }
            }

            if (paymentLabelsList.isNotEmpty()) {
                for (percent in percentages!!) {
                    val codeLabel = "P$percent"
                    val declaredField = Constants.javaClass.getDeclaredField(codeLabel)
                    if (declaredField[0].toString().split(",").toList()
                            .any { paymentLabelsList.contains(it) }
                    ) {
                        percentageFlag = true
                        depositLabel = declaredField[0].toString()
                        break
                    }
                }
            }

            cart.items?.forEach { cartItems ->
                cartItems.hotel.forEach { hotel ->
                    when {
                        hotel.room?.none { r -> r.roomCode.equals(Constants.GDP1N) } == true && hotel.room?.none { r ->
                            r.roomCode.equals(
                                Constants.GDPFN
                            )
                        } == true -> {
                            if (percentageFlag) {
                                for (room in hotel.room!!) {
                                    val totalRoomAmount = if(!isLabelChange && cart.items.first().hotel.first().promoType.equals(COUPON_PROMO_TYPE, ignoreCase = true)){
                                        cart.items.first().hotel.first().grandTotal!!
                                    }else{
                                        room.cost + room.tax?.amount!!
                                    }

                                    val percent: Int = if (depositLabel?.substring(1, 4)?.all { char -> char.isDigit() } == true) {
                                        depositLabel.substring(1, 4).toInt()
                                    } else {
                                        depositLabel?.substring(1, 3)?.toInt()!!
                                    }
                                    room.roomDepositAmount =
                                        roundDecimals(totalRoomAmount.div(100).times(percent))
                                }
                            }
                        }
                    }
                }
            }

            if (paymentLabelsList.isNotEmpty()) {

                when {
                    Constants.GDP1N.split(",").toList().any { paymentLabelsList.contains(it) } -> {
                        log.info("$${cart._id}  " + Constants.GDP1N)
                        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)){
                            depositAmount = roundDecimals(setPaymentLabelsInfo(paymentLabelsInfo, cart, Constants.GDP1N, isLabelChange))
                            if (cart.items?.get(0)?.totalPrice == depositAmount) {
                                paymentLabelsInfo.payDeposit = false
                                paymentLabelsInfo.payFull = false
                                paymentLabelsInfo.payNow = true
                            }
                        }else {
                            setPaymentInternationalLabels(paymentLabelsInfo)
                        }
                    }
                    Constants.GDPFN.split(",").toList().any { paymentLabelsList.contains(it) } -> {
                        log.info("$${cart._id}  " + Constants.GDPFN)
                        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
                            paymentLabelsInfo.payNow = true
                        }else{
                            setPaymentInternationalLabels(paymentLabelsInfo)
                        }
                    }
                    percentageFlag -> {
                        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
                            depositAmount =
                                roundDecimals(setPaymentLabelsInfo(paymentLabelsInfo, cart, depositLabel!!, isLabelChange))
                        }else{
                            setPaymentInternationalLabels(paymentLabelsInfo)
                        }
                    }
                    Constants.GAGCO.split(",").toList().any { paymentLabelsList.contains(it) } -> {
                        log.info("$${cart._id}  " + Constants.GAGCO)
                        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
                            paymentLabelsInfo.confirmBooking = true
                            cart.items?.first()?.isDepositAmount = false
                        }else{
                            paymentLabelsInfo.payAtHotel = false
                            paymentLabelsInfo.confirmBooking = true
                            paymentLabelsInfo.payDeposit = false
                            paymentLabelsInfo.payNow = false
                            paymentLabelsInfo.payFull = false
                            paymentLabelsInfo.isInternational = true
                        }
                    }
                    else -> {
                        log.info("${cart._id} " + Constants.GCC)
                        if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
                            paymentLabelsInfo.payNow = true
                            paymentLabelsInfo.payAtHotel = true
                            cart.items?.first()?.isDepositAmount = false
                        }else{
                            setPaymentInternationalLabels(paymentLabelsInfo)
                        }
                    }
                }
            }
            if (depositAmount > 0) {
                cart.items?.first()?.totalDepositAmount = depositAmount
                cart.items?.first()?.isDepositAmount = true
            }else{
                cart.items?.first()?.isDepositAmount = false
            }
            if(cart.items?.first()?.isDepositAmount == true){
                cart.items.first().payableAmount = depositAmount
                cart.priceSummary?.totalPayableAmount = depositAmount
                cart.paymentDetails?.first()?.txnNetAmount = depositAmount
                cart.items.first().balancePayable = if(!isLabelChange && cart.items.first().hotel.first().promoType.equals(COUPON_PROMO_TYPE, ignoreCase = true)){
                    cart.items.first().hotel.first().grandTotal!!.minus(depositAmount)
                }else{cart.items.first().totalPrice.minus(depositAmount)}
            }

            if(paymentLabelsInfo.confirmBooking || paymentLabelsInfo.isInternational){
                cart.paymentDetails?.first()?.paymentType = CC_AVENUE
            }else{
                cart.paymentDetails?.first()?.paymentType = JUS_PAY
            }

        cartRepository.updateCart(cart._id,cart)
        log.info("updated cart ${cart.json}")

        val cartResponse = DataMapperUtils.mapGetCartResponse(cart, null)
        cartResponse.cancelPolicyDescription=cancelPolicyDescription

        return DataMapperUtils.mapPaymentLabelResponse(paymentLabelsInfo,cartResponse)
    }

    private fun setPaymentInternationalLabels(paymentLabelsInfo: PaymentLabels?) {
        paymentLabelsInfo?.payAtHotel = true
        paymentLabelsInfo?.isInternational = true
    }

    private fun setPaymentLabelsInfo(paymentLabelsInfo: PaymentLabels?, cart: Cart, label: String, isLabelChange: Boolean): Double {
        if (label.substring(1, 4).all { char -> char.isDigit() }) {
            paymentLabelsInfo?.payNow = true
        }else {
            paymentLabelsInfo?.payDeposit = true
            paymentLabelsInfo?.payFull = true
        }
        var depositAmount = 0.0

        if (label == Constants.GDP1N) {
            val rooms: ArrayList<Room> = arrayListOf()
            cart.items?.forEach { cartItems ->
                rooms.addAll(cartItems.hotel[0].room!!.stream().filter { r -> r.roomCode.equals(label) }.toList())
            }
            for(room in rooms){
                depositAmount += room.roomDepositAmount
                paymentLabelsInfo?.depositAmount = roundDecimals(depositAmount)
            }
        } else {
            val percent = if (label.substring(1, 4).all { char -> char.isDigit() }) {
                label.substring(1, 4).toInt()
            } else {
                label.substring(1, 3).toInt()
            }

            paymentLabelsInfo?.depositAmount = if(!isLabelChange && cart.items?.first()?.hotel?.first()?.promoType.equals(COUPON_PROMO_TYPE, ignoreCase = true)){
                (cart.items?.first()?.hotel?.first()?.grandTotal?.div(100))?.times(percent)?.let { roundDecimals(it) }
            } else{
                (cart.priceSummary?.totalPrice?.div(100))?.times(percent)?.let { roundDecimals(it) }}

            depositAmount = paymentLabelsInfo?.depositAmount!!

        }
        val payLabels = label.split(",")
        for (payLabel in payLabels) {
            for (cartItems in cart.items!!) {
                if (cartItems.hotel[0].room!!.stream().filter { r -> r.roomCode.equals(payLabel) }
                        .findFirst().isPresent) {
                    paymentLabelsInfo?.gccRemarks =
                        cartItems.hotel[0].room!!.stream().filter { r -> r.roomCode.equals(payLabel) }
                            .findFirst().get().bookingPolicyDescription
                    break
                }
            }
        }
        return depositAmount
    }
    suspend fun getDiscountPrice(discountRequest: DiscountRequest): CartResponse{
        val cart: Cart? = cartRepository.findCartByCustomerHash(discountRequest.customerHash)
        if(cart != null){
            var totalDiscountPrice = 0.0
            var totalBasePrice = 0.0
            var totalTaxPrice = 0.0
            var roomGrandTotal = 0.0
            var hotelGrandTotal = 0.0
            var oldTotalBasePrice = 0.0
            var totalPrice = 0.0
            val countryCode= cart.items?.first()?.hotel?.first()?.country
            cart.items?.first()?.hotel?.first()?.room?.forEach {
                for((key,value) in discountRequest.discountPrices){
                    log.info("key $key and value $value")
                    if(key == it.roomNumber){
                        val (discountPrice, discountTax) = value
                        it.couponDiscountValue = discountPrice
                        roomGrandTotal = it.cost + discountPrice + discountTax
                        hotelGrandTotal += roomGrandTotal
                        it.grandTotal = roomGrandTotal
                        totalDiscountPrice += discountPrice
                        totalBasePrice += it.cost.plus(discountPrice)
                        totalTaxPrice += discountTax
                        it.tax?.amount = discountTax
                        it.tax?.breakDown = discountRequest.tax.breakDown?.map { b ->
                            BreakDown(
                                amount = b.amount,
                                code = b.code
                            )
                        } as MutableList<BreakDown>?

                        oldTotalBasePrice += it.cost
                    }
                }
                it.daily = discountRequest.daily
            }
            log.info("roomGrandTotal $roomGrandTotal, hotelGrandTotal $hotelGrandTotal, totalDiscountPrice $totalDiscountPrice, totalBasePrice $totalBasePrice, " +
                    "totalTaxPrice $totalTaxPrice, oldTotalBasePrice $oldTotalBasePrice")
            totalPrice = totalBasePrice.plus(totalTaxPrice)
            if(countryCode.equals(COUNTRY_CODE,ignoreCase = true)) {
                hotelGrandTotal = roundDecimals(hotelGrandTotal)
                totalPrice = roundDecimals(totalPrice)
            }
            cart.items?.first()?.hotel?.first()?.grandTotal = hotelGrandTotal
            cart.items?.first()?.hotel?.first()?.totalTaxPrice = totalTaxPrice
            cart.items?.first()?.totalCouponDiscountValue = totalDiscountPrice
            //cart.items?.first()?.totalPrice =  oldTotalBasePrice.plus(totalTaxPrice)
            cart.items?.first()?.tax =  totalTaxPrice
            cart.items?.first()?.payableAmount = totalPrice
            //cart.priceSummary?.totalPrice = oldTotalBasePrice.plus(totalTaxPrice)
            cart.priceSummary?.totalPayableAmount = totalPrice
            cart.paymentDetails?.first()?.txnNetAmount = totalPrice

            cartRepository.saveCart(cart)
            val cartPaymentLabelsRes = getPaymentLabels(cart, false)
            if(discountRequest.orderId.isNotEmpty()) {
                val paymentMethod = if(cartPaymentLabelsRes.paymentLabels?.confirmBooking == true || cartPaymentLabelsRes.paymentLabels?.isInternational == true){
                    PAY_AT_HOTEL
                }else{
                    PAY_ONLINE
                }
                updateOrderRoomDetails(cartPaymentLabelsRes, discountRequest.orderId, paymentMethod)
            }
            return cartPaymentLabelsRes.cartDetails

        }else throw HttpResponseException(CART_NOT_FOUND_ERR_MSG, HttpStatusCode.BadRequest)
    }

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(Constants.SECURITY_ENCRYPT_ALGORITH)
        val secretKey: Key = SecretKeySpec(prop.dataEncryptionAndDecryptionKey.toByteArray(), Constants.AES)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance(Constants.SECURITY_ENCRYPT_ALGORITH)
        val secretKey: Key = SecretKeySpec(prop.dataEncryptionAndDecryptionKey.toByteArray(), Constants.AES)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val encryptedBytes = Base64.getDecoder().decode(encryptedText)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}


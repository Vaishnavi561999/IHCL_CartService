package com.ihcl.cart.utils

import com.ihcl.cart.model.dto.request.*
import com.ihcl.cart.model.dto.response.*
import com.ihcl.cart.model.schema.*
import com.ihcl.cart.model.schema.Hotel
import com.ihcl.cart.model.schema.Room
import com.ihcl.cart.service.v1.CartService
import com.ihcl.cart.utils.Constants.GIFT_CARD
import com.ihcl.cart.utils.Constants.INITIATED
import com.ihcl.cart.utils.Constants.JUS_PAY
import com.ihcl.cart.utils.Constants.NEU_COINS
import com.ihcl.cart.utils.Constants.PENDING
import com.ihcl.cart.utils.Constants.SUCCESS
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

object DataMapperUtils {
    val cartService by KoinJavaComponent.inject<CartService>(CartService::class.java)
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun mapRateCodeRequest(cartRequest: CartRequest): RateCodeRequest {
        var adultCount = 0
        var childCount = 0
        val roomCount = cartRequest.hotel[0].room.size
        cartRequest.hotel[0].room.forEach {
            adultCount += it.adult
            childCount += it.children
        }

        return RateCodeRequest(
            hotelId = cartRequest.hotel[0].hotelId,
            adults = adultCount.toString(),
            children = childCount.toString(),
            endDate = cartRequest.hotel[0].checkOut,
            numRooms = roomCount.toString(),
            startDate = cartRequest.hotel[0].checkIn,
            rateCode = cartRequest.hotel.first().room.first().rateCode
        )
    }

    fun mapModifyBookingRateCodeRequest(hotelId: String, modifyBooking: ModifyBooking, room: Int):RateCodeRequest{
        val roomCount = 1
        val adultCount = modifyBooking.modifyBookingDetails[room].adult
        val childCount = modifyBooking.modifyBookingDetails[room].children

        return RateCodeRequest(
            hotelId = hotelId,
            adults = adultCount.toString(),
            children = childCount.toString(),
            endDate = modifyBooking.modifyBookingDetails[room].checkOut,
            numRooms = roomCount.toString(),
            startDate = modifyBooking.modifyBookingDetails[room].checkIn,
            rateCode = modifyBooking.modifyBookingDetails[room].rateCode
        )
    }

    fun mapCartSchema(
        customerHash: String,
        request: CartRequest,
        roomInfo: RoomInfoDetails
    ): Cart {

        log.info("Cart request ${request.json}")
        val price = roomInfo.roomCost+roomInfo.taxAmount?.amount!!
        log.info("room cost ${roomInfo.roomCost} tax cost ${roomInfo.taxAmount}")
        val complementaryBasePrice = if(request.hotel.first().voucherRedemption?.isComplementary == true){
            0.0
        }else roomInfo.roomCost
        val payableAmount = roundDecimals(complementaryBasePrice) + roundDecimals(roomInfo.taxAmount.amount!!)
        val voucherRedemption= if(request.hotel.first().voucherRedemption != null){
            log.info("voucher ${request.hotel.first().voucherRedemption}")
            request.hotel.first().voucherRedemption
        }else null

        val promoType = if(!request.hotel.first().promoType.isNullOrEmpty()){
            request.hotel.first().promoType
        }else null

        val promoCode = if(!request.hotel.first().promoCode.isNullOrEmpty()){
            request.hotel.first().promoCode
        }else null

        val hotelDetails: MutableList<Hotel> = request.hotel.map {hotel ->
            Hotel(
                hotelId = hotel.hotelId,
                hotelName = hotel.hotelName,
                hotelAddress = hotel.hotelAddress,
                pinCode = hotel.pinCode,
                state = hotel.state,
                checkIn = hotel.checkIn,
                checkOut = hotel.checkOut,
                bookingNumber = null,
                mobileNumber = hotel.mobileNumber,
                emailId = hotel.emailId,
                voucherRedemption = voucherRedemption,
                promoType = promoType,
                promoCode = promoCode,
                revisedPrice = 0.0,
                grandTotal = 0.0,
                totalTaxPrice = 0.0,
                totalBasePrice = 0.0,
                amountPaid = 0.0,
                country = hotel.country,
                storeId = hotel.storeId,
                hotelSponsorId = hotel.hotelSponsorId,
                synxisId = roomInfo.synxisId,
                bookingCancelRemarks = null,
                room = hotel.room.map {
                    Room(
                        isPackage = it.isPackageCode,
                        roomId = it.roomId,
                        roomTypeCode = it.roomTypeCode,
                        roomName = it.roomName,
                        roomNumber = it.roomNumber,
                        cost = roomInfo.roomCost,
                        children = it.children,
                        adult = it.adult,
                        rateCode = it.rateCode,
                        currency = it.currency,
                        rateDescription = "",
                        roomDescription = "",
                        packageCode = it.packageCode,
                        isServiceable = it.isServiceable,
                        message = it.message,
                        packageName = it.packageName,
                        roomImgUrl = it.roomImgUrl,
                        tax = roomInfo.taxAmount,
                        checkIn = hotel.checkIn,
                        checkOut = hotel.checkOut,
                        bookingPolicyDescription = roomInfo.bookingPolicyDescription,
                        daily =roomInfo.dailyRates,
                        cancelPolicyDescription = roomInfo.cancelPolicyDescription,
                        changePrice = null,
                        changeTax = null,
                        modifyBooking = null,
                        confirmationId = null,
                        description = roomInfo.description,
                        detailedDescription = roomInfo.detailedDescription,
                        roomCode = roomInfo.roomCode,
                        roomDepositAmount = roomInfo.amountWithTaxesFees,
                        grandTotal = price,
                        paidAmount = 0.0,
                        penaltyAmount = null,
                        penaltyDeadLine = null,
                        noOfNights = noOfNights(hotel.checkIn, hotel.checkOut),
                        cancelPolicyCode = roomInfo.cancelPolicyCode
                    )
                }.toMutableList(),
                isSeb = hotel.isSeb,
                sebRequestId = hotel.sebRequestId
            )
        }.toMutableList()
        log.info("hotel details for voucher${hotelDetails}")

        val paymentDetails = mutableListOf( PaymentDetails(
            paymentType = JUS_PAY,
            paymentMethod = "",
            paymentMethodType = "",
            txnGateway = 0,
            txnId = "",
            ccAvenueTxnId = "",
            txnNetAmount = payableAmount,
            txnStatus = INITIATED,
            txnUUID = "",
            cardNumber = "",
            cardPin = "",
            preAuthCode = "",
            batchNumber = "0",
            approvalCode = "",
            transactionId = 0,
            transactionDateAndTime = "",
            cardNo = "",
            nameOnCard = "",
            externalId = "",
            pointsRedemptionsSummaryId = "",
            redemptionId = "",
            userId = "",
            expiryDate = ""
        ))
        return Cart(
            _id = customerHash,
            items = mutableListOf(
                CartItems(
                    hotel = hotelDetails,
                    basePrice = complementaryBasePrice,
                    tax = roomInfo.taxAmount.amount!!,
                    totalPrice = payableAmount,
                    payableAmount = payableAmount,
                    newTotalPrice = null,
                    modifiedPaymentDetails = null,
                    category = request.category,
                    totalDepositAmount = 0.0,
                    isDepositAmount = false,
                    balancePayable = 0.0,
                    totalCouponDiscountValue = 0.0)
            ),
            paymentDetails = paymentDetails,
            priceSummary = PriceSummary(
                totalPrice = payableAmount,
                giftCardPrice = 0.0,
                neuCoins = 0.0,
                voucher = 0.0,
                totalPayableAmount = payableAmount
            )
        )
    }

    fun mapRoomProduct(
        request: CartRequest,
        roomInfo: RoomInfoDetails
    ): MutableList<Room> {

        val room = request.hotel.map {
            Room(
                isPackage = it.room[0].isPackageCode,
                roomId = it.room[0].roomId,
                roomTypeCode = it.room[0].roomTypeCode,
                roomName = it.room[0].roomName,
                roomNumber = it.room[0].roomNumber,
                cost = roomInfo.roomCost,
                rateCode = it.room[0].rateCode,
                currency = it.room[0].currency,
                children = it.room[0].children,
                adult = it.room[0].adult,
                rateDescription = "",
                roomDescription = "",
                packageCode = it.room[0].packageCode,
                isServiceable = it.room[0].isServiceable,
                message = it.room[0].message,
                packageName = it.room[0].packageName,
                roomImgUrl = it.room[0].roomImgUrl,
                changePrice = null,
                changeTax = null,
                modifyBooking = null,
                tax = roomInfo.taxAmount,
                checkOut = request.hotel[0].checkOut,
                checkIn = request.hotel[0].checkIn,
                bookingPolicyDescription = roomInfo.bookingPolicyDescription,
                daily = roomInfo.dailyRates,
                cancelPolicyDescription = roomInfo.cancelPolicyDescription,
                confirmationId = null,
                description = roomInfo.description,
                detailedDescription = roomInfo.detailedDescription,
                roomCode = roomInfo.roomCode,
                roomDepositAmount = roomInfo.amountWithTaxesFees,
                grandTotal = roomInfo.roomCost + roomInfo.taxAmount?.amount!!,
                paidAmount = 0.0,
                penaltyDeadLine = null,
                penaltyAmount = null,
                noOfNights = noOfNights(request.hotel[0].checkIn, request.hotel[0].checkOut),
                cancelPolicyCode = roomInfo.cancelPolicyCode,
                createdTimestamp = Date()
            )
        }.toMutableList()
        return room
    }


    fun mapCartEmptyResponse(id: String): CartResponse {

        return CartResponse(
            cartId = id,
            items = null,
            basePrice =null,
            tax = null,
            totalPrice = null,
            createdDate = null,
            modifiedDate = null,
            payableAmount = null,
            paymentSummary = null,
            paymentDetails = null,
            modifiedPaymentDetails = null,
            totalPriceChange = null,
            totalTaxChange = null,
            isDepositAmount = false,
            totalDepositAmount = 0.0,
            balancePayable = 0.0,
            totalCouponDiscountValue = 0.0)
    }

    fun mapGetCartResponse(cart: Cart?, errorMessage: ErrorMessage?): CartResponse {
       val message = errorMessage ?: ErrorMessage(SUCCESS_STATUS_CODE, SUCCESS)
        var totalChangePrice = 0.0
        var totalChangeTax = 0.0
        val hotelOutline: MutableList<HotelDetails> =
            cart?.items?.get(0)?.hotel?.map { hotel ->
                HotelDetails(
                    hotelId = hotel.hotelId,
                    hotelName = hotel.hotelName,
                    hotelAddress = hotel.hotelAddress,
                    pinCode = hotel.pinCode,
                    state = hotel.state,
                    checkIn = hotel.checkIn,
                    checkOut = hotel.checkOut,
                    bookingNumber = hotel.bookingNumber,
                    promoCode = hotel.promoCode,
                    promoType = hotel.promoType,
                    voucherRedemption = hotel.voucherRedemption,
                    mobileNumber = hotel.mobileNumber,
                    emailId = hotel.emailId,
                    revisedPrice = hotel.revisedPrice,
                    grandTotal = hotel.grandTotal,
                    totalBasePrice = hotel.totalBasePrice,
                    totalTaxPrice = hotel.totalTaxPrice,
                    amountPaid = hotel.amountPaid,
                    country = hotel.country,
                    storeId = hotel.storeId,
                    hotelSponsorId = hotel.hotelSponsorId,
                    synxisId = hotel.synxisId,
                    bookingCancelRemarks = hotel.bookingCancelRemarks,
                    room = hotel.room?.map {
                        RoomDetails(
                            isPackageCode = it.isPackage,
                            roomId = it.roomId,
                            roomName = it.roomName,
                            roomNumber = it.roomNumber,
                            roomType = it.roomTypeCode,
                            roomDescription = it.roomDescription,
                            cost = it.cost,
                            children = it.children,
                            adult = it.adult,
                            rateCode = it.rateCode,
                            currency = it.currency,
                            rateDescription = it.rateDescription,
                            isServiceable = it.isServiceable,
                            message = it.message,
                            packageCode = it.packageCode,
                            packageName = it.packageName,
                            roomImgUrl = it.roomImgUrl,
                            changePrice = it.changePrice,
                            changeTax = it.changeTax,
                            checkOut = it.checkOut,
                            checkIn = it.checkIn,
                            couponDiscountValue = it.couponDiscountValue!!,
                            tax = TaxInfo(
                                amount = it.tax?.amount,
                                breakDown = it.tax?.breakDown?.map {breakDown ->
                                    BreakDownInfo(
                                        amount = breakDown.amount,
                                        code = breakDown.code
                                    )
                                }

                            ),
                            confirmationId = it.confirmationId,
                            bookingPolicyDescription = it.bookingPolicyDescription,
                            daily = it.daily,
                            cancelPolicyDescription = it.cancelPolicyDescription,
                            description = it.description,
                            detailedDescription = it.detailedDescription,
                            roomCode = it.roomCode,
                            roomDepositAmount = it.roomDepositAmount,
                            grandTotal = it.grandTotal,
                            paidAmount = it.paidAmount,
                            penaltyDeadLine = it.penaltyDeadLine,
                            penaltyAmount = it.penaltyAmount,
                            status = it.status,
                            noOfNights = it.noOfNights,
                            cancelRefundableAmount = it.cancelRefundableAmount,
                            cancelPayableAmount = it.cancelPayableAmount,
                            cancellationTime = it.cancellationTime,
                            cancelRemark = it.cancelRemark,
                            cancellationId = it.cancellationId,
                            penaltyApplicable = it.penaltyApplicable,
                            createdTimestamp = it.createdTimestamp,
                            modifiedTimestamp = it.modifiedTimestamp,
                            modifyBooking = it.modifyBooking?.let { mb->
                                ModifiedRoom(
                                    isPackageCode = mb.isPackage,
                                    roomId = mb.roomId,
                                    roomName = mb.roomName,
                                    roomNumber = mb.roomNumber,
                                    roomType = mb.roomTypeCode,
                                    roomDescription = mb.roomDescription,
                                    cost = mb.cost,
                                    children = mb.children,
                                    adult = mb.adult,
                                    rateCode = mb.rateCode,
                                    currency = mb.currency,
                                    rateDescription = mb.rateDescription,
                                    isServiceable = mb.isServiceable,
                                    message = mb.message,
                                    packageCode = mb.packageCode,
                                    packageName = mb.packageName,
                                    roomImgUrl = mb.roomImgUrl,
                                    tax = TaxInfo(
                                        amount = mb.tax?.amount,
                                        breakDown = mb.tax?.breakDown?.map {
                                            BreakDownInfo(
                                                amount = it.amount,
                                                code = it.code
                                            )
                                        }
                                    ),
                                    daily = mb.daily,
                                    checkIn = mb.checkIn,
                                    checkOut = mb.checkOut,
                                    confirmationId = mb.confirmationId,
                                    bookingPolicyDescription = mb.bookingPolicyDescription,
                                    cancelPolicyDescription = mb.cancelPolicyDescription,
                                    description = mb.description,
                                    detailedDescription = mb.detailedDescription,
                                    grandTotal = mb.grandTotal,
                                    paidAmount = mb.paidAmount,
                                    roomCode = it.roomCode,
                                    roomDepositAmount = it.roomDepositAmount,
                                    penaltyAmount = mb.penaltyAmount,
                                    penaltyDeadLine = mb.penaltyDeadLine,
                                    status = mb.status,
                                    noOfNights = mb.noOfNights,
                                    createdTimestamp = mb.createdTimestamp,
                                    modifiedTimestamp = mb.modifiedTimestamp
                                )
                            })
                    }!!.toMutableList(),
                    isSeb = hotel.isSeb,
                    sebRequestId = hotel.sebRequestId
                )
            }!!.toMutableList()
        log.info("hotel outline ${hotelOutline.json}")
        val totalPrice = roundDecimals(cart.items[0].totalPrice)
        val payableAmount = if(cart.items[0].modifiedPaymentDetails!=null) {
            roundDecimals(cart.items[0].payableAmount)
        }else cart.priceSummary!!.totalPayableAmount
        if(cart.items.first().modifiedPaymentDetails != null){
            cart.items.first().hotel.first().room?.forEach {
                if(it.changePrice !=null && it.changeTax !=null){
                    totalChangePrice += it.changePrice!!
                    totalChangeTax += it.changeTax!!
                }

            }
        }

        return CartResponse(
            cartId = cart._id,
            items = listOf(
                CartResponseItems(
                    category = cart.items[0].category,
                    hotel = hotelOutline,
                )
            ),
            basePrice = cart.items[0].basePrice,
            tax = cart.items[0].tax,
            totalPrice = totalPrice,
            payableAmount = payableAmount,
            totalTaxChange = totalChangeTax,
            totalPriceChange = totalChangePrice,
            modifiedPaymentDetails = cart.items[0].modifiedPaymentDetails?.let {
                ModifiedPaymentDetails(
                    modifiedBasePrice =  it.modifiedBasePrice,
                    modifiedTax = it.modifiedTax,
                    modifiedTotalPrice = it.modifiedTotalPrice,
                    modifiedPayableAmount = it.modifiedPayableAmount
                )
            },
            createdDate = cart.createdTimestamp.toString(),
            modifiedDate = cart.modifiedTimestamp.toString(),
            paymentSummary =
            PaymentSummary(
                totalPrice = cart.priceSummary!!.totalPrice,
                giftCardPrice = cart.priceSummary!!.giftCardPrice,
                neuCoins = cart.priceSummary!!.neuCoins,
                voucher = cart.priceSummary!!.voucher,
                totalPayableAmount = cart.priceSummary!!.totalPayableAmount
            ),
            isDepositAmount = cart.items.first().isDepositAmount,
            totalDepositAmount = cart.items.first().totalDepositAmount,
            modifiedPayableAmount = cart.items.first().modifiedPayableAmount,
            refundableAmount = cart.items.first().refundAmount,
            balancePayable = cart.items.first().balancePayable,
            paymentMethod =  cart.paymentMethod,
            totalCouponDiscountValue = cart.items.first().totalCouponDiscountValue,
            paymentDetails = cart.paymentDetails?.map {
                PaymentDetailsInfo(
                    paymentMethod = it.paymentMethod,
                    paymentType = it.paymentType,
                    paymentMethodType = it.paymentMethodType,
                    txnGateway = it.txnGateway,
                    txnId = it.txnId,
                    txnNetAmount = it.txnNetAmount,
                    txnStatus = it.txnStatus,
                    txnUUID = it.txnUUID,
                    cardNumber = it.cardNumber?.let { it1 -> cartService.decrypt(it1) },
                    cardPin = it.cardPin?.let { it1 -> cartService.decrypt(it1) },
                    preAuthCode = it.preAuthCode,
                    batchNumber = it.batchNumber,
                    approvalCode = it.approvalCode,
                    transactionId = it.transactionId,
                    transactionDateAndTime = it.transactionDateAndTime,
                    cardNo = it.cardNo,
                    nameOnCard = it.nameOnCard,
                    externalId = it.externalId,
                    pointsRedemptionsSummaryId = it.pointsRedemptionsSummaryId,
                    redemptionId = it.redemptionId,
                    userId = it.userId,
                    expiryDate = it.expiryDate,
                    ccAvenueTxnId = it.ccAvenueTxnId
                )
            }?.toMutableList(),
            errorMessage = message
        )
    }
    fun mapJusPayPaymentDetailsOfGC(amount: Double?): PaymentDetails {
        return PaymentDetails(
            paymentType = JUS_PAY,
            paymentMethod = "",
            paymentMethodType = "",
            txnGateway = 0,
            txnId = "",
            ccAvenueTxnId = "",
            txnNetAmount = amount,
            txnStatus = PENDING,
            txnUUID = "",
            cardNo = "",
            nameOnCard = "",
            userId = "",
            redemptionId = "",
            pointsRedemptionsSummaryId = "",
            externalId = "",
            cardNumber = "",
            cardPin = "",
            preAuthCode = "",
            batchNumber = "",
            approvalCode = "",
            transactionId = 0,
            transactionDateAndTime = "",
            expiryDate = ""
        )
    }
    fun mapGiftCardCartResponse(cart: GiftCardCart): GiftCardCartResponse {
        val paymentDetails = cart.paymentDetails?.map {
            PaymentDetails(
                it.paymentType,
                it.paymentMethod,
                it.paymentMethodType,
                it.txnGateway,
                it.txnId,
                it.ccAvenueTxnId,
                it.txnNetAmount,
                it.txnStatus,
                it.txnUUID,
                it.cardNo,
                it.nameOnCard,
                it.userId,
                it.redemptionId,
                it.pointsRedemptionsSummaryId,
                it.externalId,
                it.cardNumber,
                it.cardPin,
                it.preAuthCode,
                it.batchNumber,
                it.approvalCode,
                it.transactionId,
                it.transactionDateAndTime,
                it.expiryDate
            )
        }
        return GiftCardCartResponse(
            _id = cart._id,
            items = GCCartResponseItems(
                cart.items?.isMySelf,
                cart.items?.category,
                cart.items?.quantity,
                giftCardDetails = listOf(
                    GCDetails(
                        cart.items?.giftCardDetails?.first()?.amount,
                        cart.items?.giftCardDetails?.first()?.sku,
                        cart.items?.giftCardDetails?.first()?.type,
                        cart.items?.giftCardDetails?.first()?.theme,
                        cart.items?.giftCardDetails?.first()?.giftCardNumber,
                        cart.items?.giftCardDetails?.first()?.giftCardPin,
                        cart.items?.giftCardDetails?.first()?.validity,
                        cart.items?.giftCardDetails?.first()?.orderId
                    )
                ),
                DeliveryMethods(
                    cart.items?.deliveryMethods?.phone,
                    cart.items?.deliveryMethods?.email,
                    cart.items?.deliveryMethods?.smsAndWhatsApp
                ),
                ReceiverAddress(
                    cart.items?.receiverAddress?.addressLine1,
                    cart.items?.receiverAddress?.addressLine2,
                    cart.items?.receiverAddress?.city,
                    cart.items?.receiverAddress?.country,
                    cart.items?.receiverAddress?.pinCode,
                    cart.items?.receiverAddress?.state
                ),
                ReceiverAddress(
                    cart.items?.receiverAddress?.addressLine1,
                    cart.items?.receiverAddress?.addressLine2,
                    cart.items?.receiverAddress?.city,
                    cart.items?.receiverAddress?.country,
                    cart.items?.receiverAddress?.pinCode,
                    cart.items?.receiverAddress?.state
                ),
                ReceiverDetails(
                    cart.items?.receiverDetails?.email,
                    cart.items?.receiverDetails?.firstName,
                    cart.items?.receiverDetails?.lastName,
                    cart.items?.receiverDetails?.message,
                    cart.items?.receiverDetails?.phone
                ),
                SenderDetails(
                    cart.items?.senderDetails?.email,
                    cart.items?.senderDetails?.firstName,
                    cart.items?.senderDetails?.lastName,
                    cart.items?.senderDetails?.phone
                )
            ),
            paymentDetails = paymentDetails?.toMutableList(),
            priceSummary = GiftCardPriceSummary(
                cart.priceSummary?.totalPrice,
                cart.priceSummary?.neuCoins,
                cart.priceSummary?.totalPayableAmount
            )
        )
    }

    fun mapPaymentLabelResponse(paymentLabels:PaymentLabels?,cartResponse:CartResponse):CartPaymentLabelResponse{
        return CartPaymentLabelResponse(
            paymentLabels = paymentLabels,
            cartDetails = cartResponse
        )
    }


    fun mapCartForCheckAvailability(cart: Cart): ReviewCartRequest {
        var roomCount = 0
        var childCount = 0
        var audultCount = 0
        var hotelId = ""
        var startDate = ""
        var endDate = ""
        cart.items?.forEach {cartItems ->
            cartItems.hotel.forEach {hotel ->
                startDate = hotel.checkIn
                endDate = hotel.checkOut
                hotelId = hotel.hotelId
                roomCount = hotel.room!!.size
                hotel.room!!.forEach {room ->
                    childCount += room.children
                    audultCount += room.adult
                }
            }
        }
        return ReviewCartRequest(
            hotelId = hotelId,
            roomCount = roomCount,
            childCount = childCount,
            adultCount = audultCount,
            promoCode = "",
            startDate = startDate,
            endDate = endDate
        )
    }


    fun mapNeuCoinsPaymentDetails(tenderModes:TenderModes,totalPayableAmount:Double): MutableList<PaymentDetails> {
        val paymentDetails = tenderModes.tenderModeDetails.map { _ ->
            PaymentDetails(
                paymentType = tenderModes.tenderMode.toString(),
                paymentMethod = NEU_COINS,
                paymentMethodType = NEU_COINS,
                txnGateway = 0,
                txnId = "",
                txnNetAmount = totalPayableAmount,
                txnStatus = PENDING,
                txnUUID = "",
                cardNumber = "",
                cardPin = "",
                preAuthCode = "",
                batchNumber = "0",
                approvalCode = "",
                transactionId = 0,
                transactionDateAndTime = "",
                cardNo = "",
                nameOnCard = "",
                externalId = "",
                pointsRedemptionsSummaryId = "",
                redemptionId = "",
                userId = "",
                expiryDate = "",
                ccAvenueTxnId = ""
            )
        }
        return paymentDetails.toMutableList()
    }
    fun mapGiftCardPaymentDetails(tenderModes:TenderModes,totalPayableAmount:Double): MutableList<PaymentDetails> {
        val paymentDetails = tenderModes.tenderModeDetails.map { tm ->
            PaymentDetails(
                paymentType = tenderModes.tenderMode.toString(),
                paymentMethod = GIFT_CARD,
                paymentMethodType = GIFT_CARD,
                txnGateway = 0,
                txnId = "",
                txnNetAmount = totalPayableAmount,
                txnStatus = PENDING,
                txnUUID = "",
                cardNumber = cartService.encrypt(tm.cardNumber),
                cardPin = cartService.encrypt(tm.cardPin),
                preAuthCode = "",
                batchNumber = "0",
                approvalCode = "",
                transactionId = 0,
                transactionDateAndTime = "",
                cardNo = "",
                nameOnCard = "",
                externalId = "",
                pointsRedemptionsSummaryId = "",
                redemptionId = "",
                userId = "",
                expiryDate = "",
                ccAvenueTxnId = ""
            )
        }
        return paymentDetails.toMutableList()
    }

    fun mapPaymentDetails(cart: Cart):MutableList<PaymentDetails>{
        val payableAmount = if(cart.items!![0].modifiedPaymentDetails!=null){
            cart.items[0].modifiedPaymentDetails!!.modifiedPayableAmount
        }else cart.items[0].payableAmount
        return mutableListOf( PaymentDetails(
            paymentType = JUS_PAY,
            paymentMethod = "",
            paymentMethodType = "",
            txnGateway = 0,
            txnId = "",
            txnNetAmount = payableAmount!!,
            txnStatus = PENDING,
            txnUUID = "",
            cardNumber = "",
            cardPin = "",
            preAuthCode = "",
            batchNumber = "0",
            approvalCode = "",
            transactionId = 0,
            transactionDateAndTime = "",
            cardNo = "",
            nameOnCard = "",
            externalId = "",
            pointsRedemptionsSummaryId = "",
            redemptionId = "",
            userId = "",
            expiryDate = "",
            ccAvenueTxnId = ""
        ))
    }
    fun mapLoyaltyPaymentDetails(payableAmount: Double):MutableList<PaymentDetails>{
        return mutableListOf( PaymentDetails(
            paymentType = JUS_PAY,
            paymentMethod = "",
            paymentMethodType = "",
            txnGateway = 0,
            txnId = "",
            txnNetAmount = payableAmount,
            txnStatus = PENDING,
            txnUUID = "",
            cardNumber = "",
            cardPin = "",
            preAuthCode = "",
            batchNumber = "0",
            approvalCode = "",
            transactionId = 0,
            transactionDateAndTime = "",
            cardNo = "",
            nameOnCard = "",
            externalId = "",
            pointsRedemptionsSummaryId = "",
            redemptionId = "",
            userId = "",
            expiryDate = "",
            ccAvenueTxnId = ""
        ))
    }

    fun mapNeuCoinsPriceSummary(totalPayableAmount: Double, cart: Cart, neuCoinBalance: Double): PriceSummary {
        return PriceSummary(
            totalPrice = cart.items?.get(0)?.totalPrice,
            giftCardPrice = cart.priceSummary!!.giftCardPrice,
            neuCoins = cart.priceSummary!!.neuCoins.plus(neuCoinBalance),
            voucher = cart.priceSummary!!.voucher,
            totalPayableAmount = totalPayableAmount
        )
    }
    fun mapNeuCoinsLoyaltyPriceSummary(totalPayableAmount: Double, cart: LoyaltyCart, neuCoinBalance: Double): LoyaltyPriceSummary {
        return LoyaltyPriceSummary(
            price = cart.priceSummary.price,
            tax = cart.priceSummary.tax,
            totalPrice = cart.priceSummary.totalPrice,
            neuCoins = cart.priceSummary.neuCoins.plus(neuCoinBalance),
            totalPayableAmount = totalPayableAmount,
            discountPercent = cart.priceSummary.discountPercent,
            discountPrice = cart.priceSummary.discountPrice,
            discountTax = cart.priceSummary.discountTax
        )
    }
    fun mapNeuCoinsGCPriceSummary(totalPayableAmount: Double, cart: GiftCardCart, neuCoinBalance: Double): GiftCardPriceSummary {
        return GiftCardPriceSummary(
            totalPrice = cart.priceSummary?.totalPrice,
            neuCoins = cart.priceSummary?.neuCoins?.plus(neuCoinBalance),
            totalPayableAmount = totalPayableAmount,
            )
    }
    fun mapGiftCardPriceSummary(totalPayableAmount:Double,cart:Cart,giftCardBalance:Double):PriceSummary{
        val priceSummary = PriceSummary(
            totalPrice = cart.items?.get(0)?.totalPrice,
            giftCardPrice = cart.priceSummary!!.giftCardPrice.plus(giftCardBalance),
            neuCoins = cart.priceSummary!!.neuCoins,
            voucher = cart.priceSummary!!.voucher,
            totalPayableAmount = totalPayableAmount
        )
        return priceSummary
    }
    fun mapModifiedNeuCoinsPriceSummary(totalPayableAmount: Double, cart: Cart, neuCoinBalance: Double): PriceSummary {
        return PriceSummary(
            totalPrice = cart.items?.get(0)?.modifiedPaymentDetails!!.modifiedTotalPrice,
            giftCardPrice = cart.priceSummary!!.giftCardPrice,
            neuCoins = cart.priceSummary!!.neuCoins.plus(neuCoinBalance),
            voucher = cart.priceSummary!!.voucher,
            totalPayableAmount = totalPayableAmount
        )
    }
    fun mapModifiedGiftCardsPriceSummary(
        totalPayableAmount: Double,
        cart: Cart,
        giftCardBalance: Double
    ): PriceSummary {
        return PriceSummary(
            totalPrice = cart.items?.get(0)?.modifiedPaymentDetails!!.modifiedTotalPrice,
            giftCardPrice = cart.priceSummary!!.giftCardPrice.plus(giftCardBalance),
            neuCoins = cart.priceSummary!!.neuCoins,
            voucher = cart.priceSummary!!.voucher,
            totalPayableAmount = totalPayableAmount
        )
    }


    fun mapCartDetails(order: Order, modifyBooking: ModifyBooking): Cart{
        log.info("enter into mapping")
        val newTotalPrice = 0.0
        val room : MutableList<Room> = order.orderLineItems[0].hotel?.rooms?.map {
            if(it.modifyBooking != null){
                log.info("enter if modify booking is not null $modifyBooking")
                val modifiedRoom = ModifiedRoomDetails(
                    isPackage = it.modifyBooking.isPackage,
                    roomId = it.modifyBooking.roomId,
                    roomName = it.modifyBooking.roomName,
                    roomNumber = it.modifyBooking.roomNumber,
                    roomTypeCode = it.modifyBooking.roomType,
                    roomDescription = it.modifyBooking.roomDescription,
                    cost = it.modifyBooking.price,
                    children = it.modifyBooking.children!!,
                    adult = it.modifyBooking.adult!!,
                    rateCode = it.modifyBooking.rateCode.toString(),
                    currency =it.modifyBooking.currency,
                    rateDescription = it.modifyBooking.rateDescription,
                    isServiceable = false,
                    message = "message",
                    packageCode = it.modifyBooking.packageCode!!,
                    packageName = it.modifyBooking.packageName!!,
                    roomImgUrl = it.modifyBooking.roomImgUrl.toString(),
                    status = it.modifyBooking.status,
                    cancellationId = it.modifyBooking.cancellationId,
                    cancelRemark = it.cancelRemark,
                    cancellationTime = it.modifyBooking.cancellationTime,
                    penaltyApplicable = it.modifyBooking.penaltyApplicable,
                    checkIn = it.modifyBooking.checkIn,
                    checkOut = it.modifyBooking.checkOut,
                    tax = Tax(
                        amount = it.modifyBooking.tax?.amount,
                        breakDown = it.modifyBooking.tax?.breakDown?.map {breakDownInfo ->
                            BreakDown(
                                amount = breakDownInfo.amount,
                                code = breakDownInfo.code
                            )
                        }
                    ),
                    confirmationId = it.modifyBooking.confirmationId,
                    bookingPolicyDescription = it.modifyBooking.bookingPolicyDescription,
                    cancelPolicyDescription = it.modifyBooking.cancelPolicyDescription,
                    description = it.modifyBooking.description,
                    detailedDescription = it.modifyBooking.detailedDescription,
                    paidAmount = it.paidAmount,
                    grandTotal = it.modifyBooking.grandTotal,
                    penaltyAmount = it.modifyBooking.penaltyAmount,
                    penaltyDeadLine = it.modifyBooking.penaltyDeadLine,
                    noOfNights = it.modifyBooking.noOfNights,
                    code = it.modifyBooking.roomCode,
                    daily = it.modifyBooking.daily,
                    createdTimestamp = it.modifyBooking.createdTimestamp,
                    modifiedTimestamp = it.modifyBooking.modifiedTimestamp
                )
                Room(
                    isPackage = it.isPackage,
                    roomId = it.roomId,
                    roomName = it.roomName,
                    roomNumber = it.roomNumber,
                    roomTypeCode = it.roomType,
                    roomDescription = it.roomDescription,
                    checkIn = it.checkIn,
                    checkOut = it.checkOut,
                    status = it.status,
                    cancellationId = it.cancellationId,
                    cancelRemark = it.cancelRemark,
                    cancellationTime = it.cancellationTime,
                    penaltyApplicable = it.penaltyApplicable,
                    tax = Tax(
                        amount = it.tax?.amount,
                        breakDown = it.tax?.breakDown?.map {breakDown ->
                            BreakDown(
                                amount =  breakDown.amount,
                                code = breakDown.code
                            )
                        }
                    ),
                    cost = it.price,
                    children = it.children!!,
                    adult = it.adult!!,
                    rateCode = it.rateCode,
                    currency = it.currency,
                    rateDescription = it.rateDescription,
                    isServiceable = false,
                    message = "message",
                    packageCode = it.packageCode!!,
                    packageName = it.packageName!!,
                    roomImgUrl = it.roomImgUrl,
                    changePrice = it.changePrice,
                    changeTax = it.changeTax,
                    modifyBooking = modifiedRoom,
                    confirmationId = it.confirmationId,
                    bookingPolicyDescription = it.bookingPolicyDescription,
                    daily =it.daily,
                    cancelPolicyDescription = it.cancelPolicyDescription,
                    description = it.description,
                    detailedDescription = it.detailedDescription,
                    roomCode = it.roomCode,
                    roomDepositAmount = it.roomDepositAmount,
                    grandTotal = it.grandTotal,
                    paidAmount = it.paidAmount,
                    penaltyDeadLine = it.penaltyDeadLine,
                    penaltyAmount = it.penaltyAmount,
                    noOfNights = it.noOfNights,
                    cancelPayableAmount =it.cancelPayableAmount,
                    cancelRefundableAmount = it.cancelRefundableAmount,
                    cancelPolicyCode = it.cancelPolicyCode,
                    createdTimestamp = it.createdTimestamp,
                    modifiedTimestamp = it.modifiedTimestamp
                )


            }else{
                log.info("enter if modify booking is null $modifyBooking and ${it.roomType} and ${it.rateCode}")
                Room(
                    isPackage = it.isPackage,
                    roomId = it.roomId,
                    roomName = it.roomName,
                    roomNumber = it.roomNumber,
                    roomTypeCode = it.roomType,
                    roomDescription = it.roomDescription,
                    checkIn = it.checkIn,
                    checkOut = it.checkOut,
                    status = it.status,
                    cancellationId = it.cancellationId,
                    cancelRemark = it.cancelRemark,
                    cancellationTime = it.cancellationTime,
                    penaltyApplicable = it.penaltyApplicable,
                    tax = Tax(
                        amount = it.tax?.amount,
                        breakDown = it.tax?.breakDown?.map {breakDown ->
                            BreakDown(
                                amount =  breakDown.amount,
                                code = breakDown.code
                            )
                        }
                    ),
                    cost = it.price,
                    children = it.children!!,
                    adult = it.adult!!,
                    rateCode = it.rateCode,
                    currency = it.currency,
                    rateDescription = it.rateDescription,
                    isServiceable = false,
                    message = "message",
                    packageCode = it.packageCode!!,
                    packageName = it.packageName!!,
                    roomImgUrl = it.roomImgUrl,
                    changePrice = null,
                    changeTax = null,
                    modifyBooking = null,
                    confirmationId = it.confirmationId,
                    bookingPolicyDescription = it.bookingPolicyDescription,
                    daily =it.daily,
                    cancelPolicyDescription = it.cancelPolicyDescription,
                    description = it.description,
                    detailedDescription = it.detailedDescription,
                    roomCode = it.roomCode,
                    roomDepositAmount = it.roomDepositAmount,
                    grandTotal = it.grandTotal,
                    paidAmount = it.paidAmount,
                    penaltyDeadLine = it.penaltyDeadLine,
                    penaltyAmount = it.penaltyAmount,
                    noOfNights = it.noOfNights,
                    cancelPayableAmount =it.cancelPayableAmount,
                    cancelRefundableAmount = it.cancelRefundableAmount,
                    cancelPolicyCode = it.cancelPolicyCode,
                    createdTimestamp = it.createdTimestamp,
                    modifiedTimestamp = it.modifiedTimestamp
                )
            }
        }!!.toMutableList()
        val hotelDetails =
            Hotel(
                hotelId =  order.orderLineItems[0].hotel?.hotelId.toString(),
                hotelName = order.orderLineItems[0].hotel?.name.toString(),
                hotelAddress = order.orderLineItems[0].hotel?.address?.landmark.toString() ,
                pinCode = order.orderLineItems[0].hotel?.address?.pinCode.toString(),
                state = order.orderLineItems[0].hotel?.address?.state.toString(),
                checkIn = order.orderLineItems[0].hotel?.checkIn.toString(),
                checkOut = order.orderLineItems[0].hotel?.checkOut.toString(),
                bookingNumber = order.orderLineItems[0].hotel?.bookingNumber,
                promoCode = order.orderLineItems[0].hotel?.promoCode,
                promoType = order.orderLineItems[0].hotel?.promoType,
                voucherRedemption = order.orderLineItems[0].hotel?.voucherRedemption,
                mobileNumber =order.orderLineItems[0].hotel?.mobileNumber,
                emailId = order.orderLineItems[0].hotel?.emailId,
                room = room,
                revisedPrice = order.orderLineItems[0].hotel?.revisedPrice,
                grandTotal = order.orderLineItems[0].hotel?.grandTotal,
                totalBasePrice = order.orderLineItems[0].hotel?.totalBasePrice,
                totalTaxPrice = order.orderLineItems[0].hotel?.totalTaxPrice,
                amountPaid = order.orderLineItems[0].hotel?.amountPaid,
                country = order.orderLineItems[0].hotel?.country,
                bookingCancelRemarks = order.bookingCancelRemarks,
                storeId = order.orderLineItems[0].hotel?.storeId,
                hotelSponsorId =order.orderLineItems[0].hotel?.hotelSponsorId,
                isSeb = order.orderLineItems[0].hotel?.isSeb,
                sebRequestId = order.orderLineItems[0].hotel?.sebRequestId
            )


        return Cart(
            _id = order.customerHash,
            items = mutableListOf(
                CartItems(
                    hotel = mutableListOf(hotelDetails),
                    newTotalPrice = newTotalPrice,
                    basePrice = order.basePrice!!,
                    tax = order.taxAmount!!,
                    totalPrice = order.gradTotal,
                    payableAmount = order.payableAmount,
                    category = order.orderType.toString(),
                    totalDepositAmount = order.orderLineItems.first().hotel?.totalDepositAmount!!,
                    isDepositAmount = order.orderLineItems.first().hotel?.isDepositAmount!!,
                    balancePayable = 0.0,
                    totalCouponDiscountValue = 0.0,
                    modifiedPaymentDetails = ModifiedPayment(
                        modifiedBasePrice = 0.0,
                        modifiedTax = 0.0,
                        modifiedTotalPrice = 0.0,
                        modifiedPayableAmount = 0.0,
                    )
                )
            ),
            paymentDetails = null,
            priceSummary = PriceSummary(
                totalPrice = 0.0,
                giftCardPrice = 0.0,
                neuCoins = 0.0,
                voucher = 0.0,
                totalPayableAmount = 0.0
            )
        )
    }
    private fun noOfNights(checkIn:String?, checkOut:String?):Int{

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val checkInDate = LocalDate.parse(checkIn, formatter).atStartOfDay()
        val checkOutDate = LocalDate.parse(checkOut, formatter).atStartOfDay()

        val noOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate)
        log.info("noOfNights $noOfNights")
        return noOfNights.toInt()
    }

/*    fun mapModifiedRoom(cart: Cart, modifyBooking: ModifyBooking, roomCost: ArrayList<Double>,
                        taxAmount: ArrayList<Double>, taxObject: ArrayList<Tax>,
                        description: ArrayList<String?>, detailedDescription: ArrayList<String?>,
                        cancelPolicyDescription:ArrayList<String?>, bookingPolicyDescription: ArrayList<String?>,
                        paymentLabels: ArrayList<String?>, list: ArrayList<List<DailyRates>?>): Cart{
        var cost: Double
        var tax: Double
        cart.items!![0].hotel.first().room!!.map { rm ->
            for(i in 0 until  modifyBooking.modifyBookingDetails.size){
                if (rm.roomNumber == modifyBooking.modifyBookingDetails[i].roomNumber) {
                    val timeStamp = if(rm.modifyBooking != null){
                        rm.modifyBooking!!.modifiedTimestamp
                    }else{
                        rm.modifiedTimestamp
                    }
                    val modifiedRoom = ModifiedRoomDetails(
                        isPackage = modifyBooking.modifyBookingDetails[i].isPackageCode,
                        roomId = modifyBooking.modifyBookingDetails[i].roomId,
                        roomTypeCode = modifyBooking.modifyBookingDetails[i].roomTypeCode,
                        roomName = modifyBooking.modifyBookingDetails[i].roomName,
                        roomNumber = modifyBooking.modifyBookingDetails[i].roomNumber,
                        checkIn = modifyBooking.modifyBookingDetails[i].checkIn,
                        checkOut = modifyBooking.modifyBookingDetails[i].checkOut,
                        cost = roomCost[i],
                        children = modifyBooking.modifyBookingDetails[i].children,
                        adult = modifyBooking.modifyBookingDetails[i].adult,
                        rateCode = modifyBooking.modifyBookingDetails[i].rateCode,
                        currency = modifyBooking.modifyBookingDetails[i].currency,
                        packageCode = modifyBooking.modifyBookingDetails[i].packageCode,
                        isServiceable = modifyBooking.modifyBookingDetails[i].isServiceable,
                        message = modifyBooking.modifyBookingDetails[i].message,
                        packageName = modifyBooking.modifyBookingDetails[i].packageName,
                        roomImgUrl = modifyBooking.modifyBookingDetails[i].roomImgUrl,
                        rateDescription = "",
                        roomDescription = "",
                        tax = taxObject[i],
                        daily = list[i],
                        confirmationId = rm.confirmationId,
                        bookingPolicyDescription = bookingPolicyDescription[i],
                        cancelPolicyDescription = cancelPolicyDescription[i],
                        description = description[i],
                        detailedDescription = detailedDescription[i],
                        code = paymentLabels[i],
                        grandTotal = roomCost[i] + taxObject[i].amount!!,
                        paidAmount = rm.paidAmount,
                        penaltyDeadLine = null,
                        penaltyAmount = null,
                        status = PENDING,
                        noOfNights = noOfNights(
                            modifyBooking.modifyBookingDetails[i].checkIn,
                            modifyBooking.modifyBookingDetails[i].checkOut
                        ),
                        createdTimestamp = timeStamp,
                        modifiedTimestamp = Date()
                    )
                    if ((rm.modifyBooking != null && (rm.modifyBooking?.status == PENDING || rm.modifyBooking?.status == "FAILED")) ||
                        (rm.modifyBooking == null && rm.status == CONFIRMED)) {
                        cost = roomCost[i] - rm.cost
                        tax = taxAmount[i] - rm.tax?.amount!!
                        rm.changePrice = cost
                        rm.changeTax = tax
                        log.info("changed Price ${rm.changePrice} and $cost and change tax ${rm.changeTax} and $tax")
                        rm.modifyBooking = modifiedRoom

                    }else {
                        cost = roomCost[i] - rm.modifyBooking!!.cost
                        tax = taxAmount[i] - rm.modifyBooking!!.tax?.amount!!
                        rm.isPackage = rm.modifyBooking?.isPackage
                        rm.roomId = rm.modifyBooking?.roomId.toString()
                        rm.roomName = rm.modifyBooking!!.roomName
                        rm.roomNumber = rm.modifyBooking!!.roomNumber
                        rm.roomTypeCode = rm.modifyBooking!!.roomTypeCode
                        rm.roomDescription = rm.modifyBooking!!.roomDescription
                        rm.checkIn = rm.modifyBooking!!.checkIn
                        rm.checkOut = rm.modifyBooking!!.checkOut
                        rm.status = rm.modifyBooking!!.status
                        rm.cancellationId = rm.modifyBooking!!.cancellationId
                        rm.cancelRemark = rm.modifyBooking!!.cancelRemark
                        rm.cancellationTime = rm.modifyBooking!!.cancellationTime
                        rm.penaltyApplicable = rm.modifyBooking!!.penaltyApplicable
                        rm.tax = Tax(
                            amount = rm.modifyBooking!!.tax?.amount,
                            breakDown = rm.modifyBooking!!.tax?.breakDown?.map {breakDown ->
                                BreakDown(
                                    amount =  breakDown.amount,
                                    code = breakDown.code
                                )
                            }
                        )
                        rm.cost = rm.modifyBooking?.cost!!
                        rm.children = rm.modifyBooking?.children!!
                        rm.adult = rm.modifyBooking?.adult!!
                        rm.rateCode = rm.modifyBooking?.rateCode
                        rm.currency = rm.modifyBooking?.currency
                        rm.rateDescription = rm.modifyBooking?.rateDescription.toString()
                        rm.isServiceable = false
                        rm.message = "message"
                        rm.packageCode = rm.modifyBooking?.packageCode.toString()
                        rm.packageName = rm.modifyBooking?.packageName.toString()
                        rm.roomImgUrl = rm.modifyBooking?.roomImgUrl
                        rm.changePrice = cost
                        rm.changeTax = tax
                        rm.confirmationId = rm.modifyBooking?.confirmationId
                        rm.bookingPolicyDescription = rm.modifyBooking?.bookingPolicyDescription
                        rm.daily =rm.modifyBooking?.daily
                        rm.cancelPolicyDescription = rm.modifyBooking?.cancelPolicyDescription
                        rm.description = rm.modifyBooking?.description
                        rm.detailedDescription = rm.modifyBooking?.detailedDescription
                        rm.roomCode = rm.modifyBooking?.code
                        rm.grandTotal = rm.modifyBooking?.grandTotal!!
                        rm.paidAmount = rm.modifyBooking?.paidAmount
                        rm.penaltyDeadLine = rm.modifyBooking?.penaltyDeadLine
                        rm.penaltyAmount = rm.modifyBooking?.penaltyAmount
                        rm.noOfNights = rm.modifyBooking?.noOfNights
                        rm.modifyBooking = modifiedRoom
                    }
                }
            }
        }
        return cart
    }*/
    fun mapDetailsToCartSchema(customerHash: String, request: GiftCardCartRequest): GiftCardCart {
        val giftCardDetails: MutableList<GCDetails> = mutableListOf()
        val txnNetAmount: Double = (request.giftCardDetails.amount * request.giftCardDetails.quantity)
        for (i in 1..request.giftCardDetails.quantity) {
            val theme: String = if (request.giftCardDetails.theme.isNullOrEmpty()) {
                Constants.THEME
            } else {
                request.giftCardDetails.theme
            }
            log.info("Theme: $theme")
            giftCardDetails.add(
                GCDetails(
                    amount = request.giftCardDetails.amount,
                    sku = request.giftCardDetails.sku,
                    type = request.giftCardDetails.type,
                    theme = theme,
                    giftCardNumber = null,
                    giftCardPin = null,
                    validity = null,
                    orderId = null
                )
            )
        }

        val paymentDetails = mutableListOf(
            PaymentDetails(
                paymentType = JUS_PAY,
                paymentMethod = "",
                paymentMethodType = "",
                txnGateway = 0,
                txnId = "",
                ccAvenueTxnId = "",
                txnNetAmount = txnNetAmount,
                txnStatus = INITIATED,
                txnUUID = "",
                cardNo = "",
                nameOnCard = "",
                userId = "",
                redemptionId = "",
                pointsRedemptionsSummaryId = "",
                externalId = "",
                cardNumber = "",
                cardPin = "",
                preAuthCode = "",
                batchNumber = "",
                approvalCode = "",
                transactionId = 0,
                transactionDateAndTime = "",
                expiryDate = ""
            )
        )
        return GiftCardCart(
            _id = customerHash,
            items = GCItems(
                isMySelf = request.self,
                deliveryMethods = request.deliveryMethods,
                category = Constants.GIFT_CARD_PURCHASE,
                quantity = request.giftCardDetails.quantity,
                giftCardDetails = giftCardDetails,
                receiverAddress = request.receiverAddress,
                senderAddress = request.senderAddress,
                receiverDetails = request.receiverDetails,
                senderDetails = request.senderDetails
            ),
            paymentDetails = paymentDetails,
            priceSummary = GiftCardPriceSummary(
                totalPrice = txnNetAmount,
                neuCoins = 0.0,
                totalPayableAmount = txnNetAmount
            )
        )
    }
    fun prepareGCCartResponse(gcCartDetails:GiftCardCart):GiftCardCartResponse{
        return GiftCardCartResponse(
            _id = gcCartDetails._id,
            items = GCCartResponseItems(
                isMySelf = gcCartDetails.items?.isMySelf,
                category = gcCartDetails.items?.category,
                quantity = gcCartDetails.items?.quantity,
                giftCardDetails = gcCartDetails.items?.giftCardDetails,
                deliveryMethods = gcCartDetails.items?.deliveryMethods,
                receiverAddress = gcCartDetails.items?.receiverAddress,
                senderAddress = gcCartDetails.items?.senderAddress,
                receiverDetails = gcCartDetails.items?.receiverDetails,
                senderDetails = gcCartDetails.items?.senderDetails
            ),
            paymentDetails = gcCartDetails.paymentDetails,
            priceSummary = gcCartDetails.priceSummary
        )
    }

    fun createLoyaltyCart(customerHash: String, request: LoyaltyRequest, priceAndDiscount: PriceAndDiscount): LoyaltyCart{
        val newPrice = priceAndDiscount.price - priceAndDiscount.discountPrice
        val newTax = priceAndDiscount.tax - priceAndDiscount.discountTax
        val totalPrice = newPrice.plus(newTax)
        val paymentDetails = mutableListOf( PaymentDetails(
            paymentType = JUS_PAY,
            paymentMethod = "",
            paymentMethodType = "",
            txnGateway = 0,
            txnId = "",
            ccAvenueTxnId = "",
            txnNetAmount = totalPrice,
            txnStatus = INITIATED,
            txnUUID = "",
            cardNumber = "",
            cardPin = "",
            preAuthCode = "",
            batchNumber = "0",
            approvalCode = "",
            transactionId = 0,
            transactionDateAndTime = "",
            cardNo = "",
            nameOnCard = "",
            externalId = "",
            pointsRedemptionsSummaryId = "",
            redemptionId = "",
            userId = "",
            expiryDate = ""
        ))
        return LoyaltyCart(
            _id = customerHash,
            items = LoyaltyCartItems(
                epicureDetails = EpicureDetails(
                    bankName = request.epicureDetails.bankName,
                    epicureType = request.epicureDetails.epicureType,
                    gravityVoucherCode = request.epicureDetails.gravityVoucherCode,
                    gravityVoucherPin = request.epicureDetails.gravityVoucherPin,
                    isBankUrl = request.epicureDetails.isBankUrl,
                    isShareHolder = request.epicureDetails.isShareHolder,
                    memberShipPurchaseType = request.epicureDetails.memberShipPurchaseType,
                    isTata = request.epicureDetails.isTata,
                    offerCode = request.epicureDetails.offerCode,
                    offerName = request.epicureDetails.offerName
                ),
            ),
           paymentDetails = paymentDetails,
            priceSummary = LoyaltyPriceSummary(
                price = priceAndDiscount.price,
                tax = priceAndDiscount.tax,
                discountPercent = priceAndDiscount.discountPercent.toInt(),
                discountPrice = priceAndDiscount.discountPrice,
                discountTax = priceAndDiscount.discountTax,
                totalPrice = totalPrice,
                neuCoins = 0.0,
                totalPayableAmount = totalPrice
            )
        )
    }
}
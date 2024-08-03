package com.ihcl.cart.utils

object Constants {
   const val CHECK_AVAILABILITY_URL = "/v1/check-availability"
   const val FETCH_ORDER_URL = "/v1/orders/fetch-order"
   const val UPDATE_ORDER_URL = "/v1/orders/update-order"
   const val RATE_CODE_URL = "/v1/rate-promo-availability"
   const val UPDATE_ORDER_BOOKING = "/v1/orders/update-order/booking"

   const val JUS_PAY = "JUS_PAY"
   const val TATA_NEU = "TATA_NEU"
   const val CC_AVENUE = "CC_AVENUE"
   const val PAY_ONLINE = "PAY ONLINE"
   const val PAY_NOW = "PAY NOW"
   const val PAY_AT_HOTEL = "PAY AT HOTEL"
   const val PAY_DEPOSIT = "PAY DEPOSIT"
   const val PAY_FULL = "PAY FULL"
   const val CONFIRM_BOOKING = "CONFIRM BOOKING"
   const val PENDING = "PENDING"
   const val INITIATED = "INITIATED"
   const val CONFIRMED = "Confirmed"
   const val NEU_COINS = "neuCoins"
   const val GIFT_CARD = "giftCard"

   const val percentages = "100,50,25"
   const val GDPFN = "GDPFN,GDPFNNR"
   const val GCC = "GCC,GCC2"
   const val GDP1N = "GDP1N"
   const val P25 = "D25N,D25R"
   const val P50 = "D50N,D50R"
   const val P100 = "D100N,D100R"
   const val GAGCO ="GTA,GCO"

   const val COUNTRY_CODE = "India"
   const val PARTIALLY_CANCELLED = "PARTIALLY CANCELLED"
   const val SUCCESS = "SUCCESS"
   const val COUPON_PROMO_TYPE = "COUPON"
   const val SECURITY_ENCRYPT_ALGORITH = "AES/ECB/PKCS5Padding"
   const val AES ="AES"

   //Gift Card
   const val GIFT_CARD_PURCHASE = "Gift_Card_Purchase"
   const val HOTEL_BOOKING = "Hotel_Booking"
   const val THEME = "physical_gift_card"
   const val CPG_ID = "CPG_1"
   const val WELLNESS = "WELLNESS"
   const val RELOAD_LIMIT_WELLNESS = "Please enter amount between 75000 to 95000"
   const val MAX_RELOAD_LIMIT = "Max reload limit amount is  more than Expected Amount"
   const val MIN_RELOAD_LIMIT = "Min reload limit amount is less than Expected Amount"
   const val CARD_TYPE_ERROR_MESSAGE = "Please Enter valid CPG Gift Card Number"
   const val MEMBERSHIP_PURCHASE = "MemberShip_Purchase"
   const val RENEWAL= "RENEWAL"
   const val PRIVILEGED= "PRIVILEGED"
   const val PREFERRED= "PREFERRED"
   const val NEW= "NEW"
   const val CORPORATE= "CORPORATE"
   const val HOTEL_SPONSER_ID_ERROR_MESSAGE="In Voucher Redemption hotelSponsorId is Mandatory"
   const val REQUEST_TIME_OUT = 180000
   const val CONNECTION_IDLE_TIME = 120000
   const val EPICURE_FIESTA= "epicure-fiesta"
   const val FACILITY= "FACILITY"
}
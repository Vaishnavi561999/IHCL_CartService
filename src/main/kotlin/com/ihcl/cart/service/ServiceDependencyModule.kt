package com.ihcl.cart.service

import com.ihcl.cart.service.v1.CartService
import com.ihcl.cart.service.v1.GiftCardCartService
import com.ihcl.cart.service.v1.LoyaltyCartService
import org.koin.dsl.module

val serviceModule = module {
    single {
        CartService()
    }
    single {
        GiftCardCartService()
    }
    single {
        LoyaltyCartService()
    }
}
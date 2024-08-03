package com.ihcl.cart.utils

import org.koin.dsl.module

val validatorModule = module {
    single {
        DataMapperUtils
    }
    single {
        ValidatorUtils
    }
}
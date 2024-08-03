package com.ihcl.cart.repository

import org.koin.dsl.module

val repositoryModule = module {
    single {
        CartRepository()
    }
}
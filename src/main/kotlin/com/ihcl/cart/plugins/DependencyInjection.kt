package com.ihcl.cart.plugins

import com.ihcl.cart.repository.repositoryModule
import com.ihcl.cart.service.serviceModule
import com.ihcl.cart.utils.validatorModule
import io.ktor.server.application.*
import org.koin.core.context.startKoin

fun Application.configureDependencyInjection() {
   startKoin {
       modules(serviceModule, validatorModule, repositoryModule)
   }
}

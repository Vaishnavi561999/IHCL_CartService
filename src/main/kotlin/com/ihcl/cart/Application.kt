package com.ihcl.cart

import com.ihcl.cart.config.Configuration
import com.ihcl.cart.plugins.*
import com.ihcl.cart.route.v1.configureCartRouting
import com.ihcl.cart.route.v1.configureStaticPagesRouting
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)
fun Application.module() {
    Configuration.initConfig(this.environment)
    configureDependencyInjection()
    configureMonitoring()
    requestHandler()
    configureHTTP()
    configureSerialization()
    configureStatusPages()
    configureCartRouting()
    configureStaticPagesRouting()
    }



package com.ihcl.cart.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun Application.configureHTTP() {
    install(HSTS) {
        includeSubDomains = true
    }
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(DefaultHeaders){
        header("Content-Security-Policy", "script-src ‘self’;")
        header("X-Content-Type-Options", "nosniff")
        header("X-XSS-Protection", "1")
    }
    install(CallId){
        val counter = AtomicInteger(0)
        generate {
            "${UUID.randomUUID()}-${counter.getAndIncrement()}"
        }
        header(HttpHeaders.XRequestId)
    }

}

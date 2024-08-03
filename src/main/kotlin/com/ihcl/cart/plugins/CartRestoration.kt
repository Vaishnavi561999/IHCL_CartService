package com.ihcl.cart.plugins


import com.ihcl.cart.utils.CUSTOMERHASH
import com.ihcl.cart.utils.ValidatorUtils
import io.ktor.server.application.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.requestHandler(){
    val log: Logger = LoggerFactory.getLogger(javaClass)
    log.info("enter into requestHandler")
    val cookieGeneration = createApplicationPlugin("CookieGenerationPlugin"){
        log.info("enter into application plugin")
       onCall { call ->
           log.info("enter into onCall")
           val customerHash = call.request.headers[CUSTOMERHASH]
           log.info("getting the headers $customerHash")
           if(customerHash.isNullOrEmpty()){
               call.request.cookies.rawCookies.forEach {
                   val cookieValue = it.value
               if(cookieValue.isEmpty()){
                   val anonymousCustomerHash: String = ValidatorUtils.getUUID()
                   call.response.cookies.append("ANONYMOUSCUSTOMERHASH",anonymousCustomerHash)

               }
               }
           }
           }
       }
}
package com.ihcl.cart.client


import com.ihcl.cart.utils.Constants
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.gson.*
import okhttp3.ConnectionPool
import okhttp3.Protocol
import java.util.concurrent.TimeUnit


val client = HttpClient(OkHttp) {
    engine {
        config {
            // this: OkHttpClient.Builder
            connectionPool(ConnectionPool(100, 5, TimeUnit.MINUTES))
            readTimeout(Constants.REQUEST_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
            connectTimeout(Constants.REQUEST_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
            writeTimeout(Constants.REQUEST_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
            retryOnConnectionFailure(true)
            protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            followRedirects(true)

        }
    }
    expectSuccess = true

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(ContentNegotiation){
        gson()
    }
    install(HttpTimeout)
}


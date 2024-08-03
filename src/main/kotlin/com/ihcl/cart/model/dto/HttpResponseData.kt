package com.ihcl.cart.model.dto

import io.ktor.http.*

data class HttpResponseData<T : Any>(val statusCode: HttpStatusCode, val data: T)
class HttpResponseException(val data: Any, val statusCode: HttpStatusCode) : Exception()

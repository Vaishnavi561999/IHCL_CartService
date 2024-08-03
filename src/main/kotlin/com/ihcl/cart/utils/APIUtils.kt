package com.ihcl.cart.utils

import com.fasterxml.jackson.databind.JsonMappingException
import com.ihcl.cart.model.dto.HttpResponseData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.math.RoundingMode
import java.util.*

private data class APIResponse<T : Any>(
    val path: String, val timestamp: Date, val statusCode: HttpStatusCode, val data: T
)

suspend fun <T : Any> ApplicationCall.fireHttpResponse(httpResponseData: HttpResponseData<T>) {
    respond(
        httpResponseData.statusCode,
        APIResponse(request.path(), Date(), httpResponseData.statusCode, httpResponseData.data)
    )
}

suspend fun <T : Any> ApplicationCall.fireHttpResponse(statusCode: HttpStatusCode, data: T) {
    respond(statusCode, APIResponse(request.path(), Date(), statusCode, data))
}

suspend fun ApplicationCall.invalidBodyStructure(exception: JsonMappingException) {
    val errorMessage: String =
        exception.message?.substringAfterLast("[")?.substringBeforeLast("]")?.plus(" is missing").toString()
            .replace("\"", "")
    fireHttpResponse(HttpStatusCode.BadRequest, errorMessage)
}

public fun roundDecimals(value: Double): Double {
    return value.toBigDecimal().setScale(
        0,
        RoundingMode.HALF_UP
    ).toDouble()
}


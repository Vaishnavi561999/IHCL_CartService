package com.ihcl.cart.plugins

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.ihcl.cart.utils.*
import com.ihcl.cart.model.exception.HttpResponseException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.serialization.SerializationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

fun Application.configureStatusPages() {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    install(StatusPages) {
        exception<JsonMappingException> { call,cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            cause.printStackTrace()
            call.invalidBodyStructure(cause)
            throw cause
        }
        exception<JsonParseException> { call,cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            cause.printStackTrace()
            call.fireHttpResponse(HttpStatusCode.BadRequest, INVALID_REQUEST_STRUCTURE_ERR_MSG)
            throw cause
        }
        exception<SerializationException> { call,cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            cause.printStackTrace()
            call.fireHttpResponse(HttpStatusCode.ExpectationFailed, SERIALIZE_ERR_MSG)
            throw cause
        }
        exception<HttpResponseException> { call, cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            cause.printStackTrace()
            call.fireHttpResponse(cause.statusCode, cause.data)
        }
        exception<TimeoutException> { call,cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            cause.printStackTrace()
            call.fireHttpResponse(HttpStatusCode.RequestTimeout, CLIENT_UNAVAILABLE_ERR_MSG)
            throw cause
        }
        exception<Throwable> { call,cause ->
            log.error(ERROR_MESSAGE_FORMAT,cause.message)
            call.fireHttpResponse(HttpStatusCode.InternalServerError, SOMETHING_WENT_WRONG_ERR_MSG)
            throw cause
        }
    }
}
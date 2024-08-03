package com.ihcl.cart.utils

import com.ihcl.cart.model.exception.HttpResponseException
import io.konform.validation.Constraint
import io.konform.validation.Invalid
import io.konform.validation.ValidationBuilder
import io.konform.validation.ValidationResult
import io.ktor.http.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object ValidatorUtils {
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    fun getUUID(): String{
        return UUID.randomUUID().toString()
    }
    fun getGuestHash(): String {
        val input = RandomStringGenerator.generateRandomString()
        val salt = UniqueHashGenerator.generateSalt()
        return UniqueHashGenerator.generateUniqueHash(input, salt)
    }
    fun ValidationBuilder<String>.notEmpty(): Constraint<String> {
        return addConstraint("Must not be empty.") { it.isNotEmpty() }
    }
    fun <T> validateRequestBody(validateResult: ValidationResult<T>) {
        if (validateResult is Invalid) {
            throw HttpResponseException(generateErrors(validateResult), HttpStatusCode.BadRequest)
        }
    }
    private fun <T> generateErrors(validationResult: ValidationResult<T>): Map<String,String> {
        val errors = mutableMapOf<String,String>()
        validationResult.errors.forEach { error ->
            errors[error.dataPath.substring(1)] = error.message
        }
        return errors
    }
    fun validateQuantity(availableQuantity : Int, requestedQuantity : Int) {
        if (availableQuantity < requestedQuantity) {
            log.info("$requestedQuantity is not present for the product, stock is $availableQuantity")
            throw HttpResponseException(NOT_ENOUGH_INVENTORY_ERR_MSG, HttpStatusCode.RequestedRangeNotSatisfiable)
        }
    }

    fun validateHotelAddress(hotelAddress: String){
        val scriptPattern = Regex("""\bscript\b""")
        val specialCharactersPattern = Regex("""[#$%^*?|<>]""")
        if(scriptPattern.containsMatchIn(hotelAddress) || specialCharactersPattern.containsMatchIn(hotelAddress)){
            throw HttpResponseException(HOTEL_ADDRESS_FORMAT_ERR_MSG, HttpStatusCode.BadRequest)
        }
    }
    fun validateDate(date: String): String{
        val pattern = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$")
        var formatDate = date
        val validateDate = pattern.matches(date)
        if(validateDate){
            val zonedDateTime = ZonedDateTime.parse(date)
            // Format the ZonedDateTime as a date string
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            formatDate = zonedDateTime.format(dateFormatter)
            log.info("formatDate $formatDate")
        }
        return formatDate
    }
  }
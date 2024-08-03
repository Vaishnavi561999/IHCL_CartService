package com.ihcl.cart.utils

import java.security.SecureRandom

object RandomStringGenerator {
    private const val CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val DEFAULT_LENGTH = 10

    fun generateRandomString(length: Int = DEFAULT_LENGTH): String {
        val random = SecureRandom()
        val randomStringBuilder = StringBuilder(length)
        repeat(length) {
            val randomCharIndex = random.nextInt(CHAR_POOL.length)
            val randomChar = CHAR_POOL[randomCharIndex]
            randomStringBuilder.append(randomChar)
        }
        return randomStringBuilder.toString()
    }
}
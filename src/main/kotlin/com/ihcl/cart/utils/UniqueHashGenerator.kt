package com.ihcl.cart.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object UniqueHashGenerator {
        private val log: Logger = LoggerFactory.getLogger(javaClass)
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 8

        fun generateUniqueHash(input: String, salt: ByteArray): String {
            val uniqueInput = input + generateUniqueIdentifier()
            val keySpec = PBEKeySpec(uniqueInput.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secretKey = keyFactory.generateSecret(keySpec)
            val hashedString = secretKey.encoded.joinToString("") { "%02x".format(it) }
            log.info("Hashed String : $hashedString")
            return hashedString
        }

        fun generateUniqueIdentifier(): String {
            // Implement your logic to generate a unique identifier here
            // For example, you can use UUID.randomUUID() or any other method
            return "UniqueIdentifier"
        }

        fun generateSalt(): ByteArray {
            // Generate a random salt
            return ByteArray(SALT_LENGTH).apply {
                for (i in indices) {
                    this[i] = (0..255).random().toByte()
                }
            }
        }
    }

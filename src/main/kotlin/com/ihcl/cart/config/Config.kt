package com.ihcl.cart.config

import com.ihcl.cart.model.dto.ConfigurationProperties
import io.ktor.server.application.*
import org.koin.core.component.getScopeId

object Configuration {
    lateinit var env: ConfigurationProperties

    fun initConfig(environment: ApplicationEnvironment){
        env = ConfigurationProperties(
            database_url = environment.config.property("ktor.database.connectionString").getString(),
            database_name = environment.config.property("ktor.database.databaseName").getString(),
            hudiniServiceHost = environment.config.property("ktor.deployment.hudiniServiceHost").getString(),
            loyaltyServiceHost = environment.config.property("ktor.deployment.loyaltyServiceHost").getString(),
            orderServiceHost = environment.config.property("ktor.deployment.orderServiceHost").getString(),
            epicurePrivilegedNewPrice = environment.config.property("ktor.deployment.epicurePrivilegedNewPrice").getString(),
            epicurePrivilegedNewTax = environment.config.property("ktor.deployment.epicurePrivilegedNewTax").getString(),
            epicurePreferredNewPrice = environment.config.property("ktor.deployment.epicurePreferredNewPrice").getString(),
            epicurePreferredNewTax = environment.config.property("ktor.deployment.epicurePreferredNewTax").getString(),
            epicureRenewalDiscount = environment.config.property("ktor.deployment.epicureRenewalDiscount").getString(),
            connectionPoolMinSize = environment.config.property("ktor.database.connectionPoolMinSize").getString().toInt(),
            connectionPoolMaxSize = environment.config.property("ktor.database.connectionPoolMaxSize").getString().toInt(),
            epicureFiestaDiscount = environment.config.property("ktor.deployment.epicureFiestaDiscount").getString().toDouble(),
            dataEncryptionAndDecryptionKey = environment.config.property("ktor.deployment.dataEncryptionAndDecryptionKey").getString()
            )

}
}

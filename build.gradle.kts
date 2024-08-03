val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project
val mongo_version: String by project
val mockito_version: String by project
val kodein_version: String by project
val resilience_version: String by project
val redis_version: String by project
val project_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.sonarqube") version "3.3"
}

sonarqube {
    properties {
      
    }
}


group = "com.ihcl.cart"
version = project_version
application {
    mainClass.set("com.ihcl.cart.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("io.ktor:ktor-server-call-id:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers:$ktor_version")
    implementation("io.ktor:ktor-server-forwarded-header:$ktor_version")
     implementation("io.ktor:ktor-server-hsts:$ktor_version")
    implementation("io.ktor:ktor-server-call-id:$ktor_version")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.12.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.litote.kmongo:kmongo-coroutine:4.5.1")
    implementation("org.kodein.di:kodein-di:$kodein_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockito_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.33.2")
    implementation("org.json:json:20220320")
    testImplementation("io.mockk:mockk:1.12.4")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")

    implementation("commons-codec:commons-codec:1.14")
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("org.jetbrains.exposed:exposed-java-time:0.37.3")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("com.zaxxer:HikariCP:2.7.8")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.konform:konform:0.4.0")
}
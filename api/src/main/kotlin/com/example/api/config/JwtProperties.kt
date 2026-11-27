package com.example.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.security.jwt")
data class JwtProperties(
    val secret: String,
    val ttl: Duration = Duration.ofHours(1),
    val issuer: String = "api",
) {
    init {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) {
            "app.security.jwt.secret must be at least $MIN_SECRET_BYTES bytes (HS256)"
        }
    }

    private companion object {
        const val MIN_SECRET_BYTES = 32
    }
}

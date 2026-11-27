package com.example.api.service

import com.example.api.config.JwtProperties
import com.example.api.model.dto.IssuedToken
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class JwtService(
    private val encoder: JwtEncoder,
    private val props: JwtProperties,
) {
    fun issue(username: String): IssuedToken {
        val now = Instant.now()
        val claims =
            JwtClaimsSet
                .builder()
                .issuer(props.issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(props.ttl))
                .id(UUID.randomUUID().toString())
                .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return IssuedToken(token, props.ttl.seconds)
    }
}

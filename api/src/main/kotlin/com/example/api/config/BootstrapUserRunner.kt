package com.example.api.config

import com.example.api.model.entity.AppUser
import com.example.api.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Dev convenience: creates the user named by `app.bootstrap.username` / `.password`
 * if it doesn't exist yet. Disabled when either is blank (the default).
 */
@Component
@Profile("!test")
class BootstrapUserRunner(
    private val users: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.bootstrap.username:}") private val username: String,
    @Value("\${app.bootstrap.password:}") private val password: String,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (username.isBlank() || password.isBlank()) return
        if (users.findByUsernameIgnoreCase(username) != null) return
        users.save(AppUser(username = username, passwordHash = passwordEncoder.encode(password)!!))
        log.info("Created bootstrap user '{}'", username)
    }
}

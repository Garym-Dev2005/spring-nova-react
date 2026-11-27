package com.example.api.service

import com.example.api.repository.AppUserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(
    private val users: AppUserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = users.findActiveByUsername(username) ?: throw UsernameNotFoundException("Unknown user")
        return User
            .withUsername(user.username)
            .password(user.passwordHash)
            .disabled(!user.enabled)
            .authorities("ROLE_USER")
            .build()
    }
}

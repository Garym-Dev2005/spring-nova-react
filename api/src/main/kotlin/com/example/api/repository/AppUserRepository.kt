package com.example.api.repository

import com.example.api.model.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AppUserRepository : JpaRepository<AppUser, UUID> {
    /** Any user, enabled or not. */
    fun findByUsernameIgnoreCase(username: String): AppUser?

    /** Active user lookup used for login; matches the partial unique index on lower(username) WHERE enabled. */
    @Query("select u from AppUser u where lower(u.username) = lower(:username) and u.enabled = true")
    fun findActiveByUsername(
        @Param("username") username: String,
    ): AppUser?
}

package com.example.api.repository

import com.example.api.model.entity.Item
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ItemRepository : JpaRepository<Item, UUID> {
    // Keyset pagination on the primary key. Ids are UUIDv7, so id order is also creation order.
    fun findAllByOrderByIdAsc(limit: Limit): List<Item>

    fun findByIdGreaterThanOrderByIdAsc(
        id: UUID,
        limit: Limit,
    ): List<Item>

    // Backwards: nearest-first, so the caller must reverse the rows.
    fun findByIdLessThanOrderByIdDesc(
        id: UUID,
        limit: Limit,
    ): List<Item>

    fun existsByIdLessThan(id: UUID): Boolean
}

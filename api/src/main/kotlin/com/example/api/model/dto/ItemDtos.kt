package com.example.api.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.hateoas.server.core.Relation
import java.time.Instant
import java.util.UUID

data class CreateItemRequest(
    @field:NotBlank @field:Size(max = 255)
    val name: String,
)

@Relation(collectionRelation = "items", itemRelation = "item")
data class ItemResponse(
    val id: UUID,
    val name: String,
    val createdAt: Instant,
)

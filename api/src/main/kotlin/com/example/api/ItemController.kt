package com.example.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

data class CreateItemRequest(
    @field:NotBlank @field:Size(max = 255)
    val name: String,
)

data class ItemResponse(
    val id: Long,
    val name: String,
    val createdAt: Instant,
)

private fun Item.toResponse() = ItemResponse(id!!, name, createdAt)

@RestController
@Tag(name = "Items")
@RequestMapping("/api/items")
class ItemController(
    private val items: ItemRepository,
) {
    @GetMapping
    @Operation(summary = "List all items")
    fun list(): List<ItemResponse> = items.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    @Operation(summary = "Get an item by id")
    fun get(
        @PathVariable id: Long,
    ): ItemResponse = items.findById(id).map { it.toResponse() }.orElseThrow { notFound(id) }

    @PostMapping
    @Operation(summary = "Create an item")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateItemRequest,
    ): ItemResponse = items.save(Item(name = request.name)).toResponse()

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: Long,
    ) {
        if (!items.existsById(id)) throw notFound(id)
        items.deleteById(id)
    }

    private fun notFound(id: Long) = ResponseStatusException(HttpStatus.NOT_FOUND, "Item $id not found")
}

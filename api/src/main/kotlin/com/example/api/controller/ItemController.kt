package com.example.api.controller

import com.example.api.model.dto.CreateItemRequest
import com.example.api.model.dto.ItemResponse
import com.example.api.service.ItemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

private const val DEFAULT_PAGE_SIZE = 20
private const val MAX_PAGE_SIZE = 100

@RestController
@Tag(name = "Items")
@RequestMapping("/api/items")
class ItemController(
    private val items: ItemService,
    private val assembler: ItemModelAssembler,
) {
    @GetMapping
    @Operation(
        summary = "List items, oldest first, one cursor-paged slice at a time",
        description =
            "Follow the `next` and `prev` links to move through the list. " +
                "Give at most one of `cursor` and `before`.",
    )
    fun list(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(required = false) before: UUID?,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") @Min(1) @Max(MAX_PAGE_SIZE.toLong()) size: Int,
    ): CollectionModel<EntityModel<ItemResponse>> {
        val page = items.list(cursor, before, size)
        val model = CollectionModel.of(page.items.map(assembler::toModel))
        model.add(
            pageLink(null, null, size).withRel(IanaLinkRelations.FIRST),
            pageLink(
                if (cursor != null) "cursor" else "before",
                cursor ?: before,
                size,
            ).withSelfRel(),
        )
        page.prevCursor?.let { model.add(pageLink("before", it, size).withRel(IanaLinkRelations.PREV)) }
        page.nextCursor?.let { model.add(pageLink("cursor", it, size).withRel(IanaLinkRelations.NEXT)) }
        return model
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an item by id")
    fun get(
        @PathVariable id: UUID,
    ): EntityModel<ItemResponse> = assembler.toModel(items.get(id))

    @PostMapping
    @Operation(summary = "Create an item")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateItemRequest,
    ): EntityModel<ItemResponse> = assembler.toModel(items.create(request))

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
    ) = items.delete(id)

    private fun pageLink(
        param: String?,
        id: UUID?,
        size: Int,
    ): Link =
        linkTo(ItemController::class.java)
            .toUriComponentsBuilder()
            .also { if (param != null && id != null) it.queryParam(param, id) }
            .queryParam("size", size)
            .build()
            .toUriString()
            .let(Link::of)
}

package com.example.api.service

import com.example.api.exception.InvalidPagingException
import com.example.api.exception.ItemNotFoundException
import com.example.api.mapper.toResponse
import com.example.api.model.dto.CreateItemRequest
import com.example.api.model.dto.CursorPage
import com.example.api.model.dto.ItemResponse
import com.example.api.model.entity.Item
import com.example.api.repository.ItemRepository
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ItemService(
    private val items: ItemRepository,
) {
    /**
     * Returns up to [size] items in ascending id order: those after [after] (exclusive), those before [before]
     * (exclusive), or the first page when both are null. Supplying both is an error.
     */
    fun list(
        after: UUID?,
        before: UUID?,
        size: Int,
    ): CursorPage<ItemResponse> {
        if (after != null &&
            before != null
        ) {
            throw InvalidPagingException("Only one of 'cursor' and 'before' may be given")
        }
        // Fetch one extra row to learn whether more items exist in the direction of travel.
        val limit = Limit.of(size + 1)
        return if (before != null) {
            val rows = items.findByIdLessThanOrderByIdDesc(before, limit)
            val page = rows.take(size).reversed().map { it.toResponse() }
            // The anchor item itself follows this page, so there is always a next page unless this one is empty.
            CursorPage(page, if (rows.size > size) page.first().id else null, page.lastOrNull()?.id)
        } else {
            val rows =
                if (after ==
                    null
                ) {
                    items.findAllByOrderByIdAsc(limit)
                } else {
                    items.findByIdGreaterThanOrderByIdAsc(after, limit)
                }
            val page = rows.take(size).map { it.toResponse() }
            val hasPrev = after != null && page.isNotEmpty() && items.existsByIdLessThan(page.first().id)
            CursorPage(page, if (hasPrev) page.first().id else null, if (rows.size > size) page.last().id else null)
        }
    }

    fun get(id: UUID): ItemResponse =
        items.findById(id).map { it.toResponse() }.orElseThrow { ItemNotFoundException(id) }

    @Transactional
    fun create(request: CreateItemRequest): ItemResponse = items.save(Item(name = request.name)).toResponse()

    @Transactional
    fun delete(id: UUID) {
        if (!items.existsById(id)) throw ItemNotFoundException(id)
        items.deleteById(id)
    }
}

package com.example.api.model.dto

import java.util.UUID

/**
 * One page of a cursor-paginated list, in ascending order.
 * [prevCursor] is the first item's id when earlier items exist, otherwise null.
 * [nextCursor] is the last item's id when later items exist, otherwise null.
 */
data class CursorPage<T>(
    val items: List<T>,
    val prevCursor: UUID?,
    val nextCursor: UUID?,
)

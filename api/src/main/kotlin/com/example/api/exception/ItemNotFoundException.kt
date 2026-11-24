package com.example.api.exception

import java.util.UUID

class ItemNotFoundException(
    id: UUID,
) : RuntimeException("Item $id not found")

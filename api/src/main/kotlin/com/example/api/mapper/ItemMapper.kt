package com.example.api.mapper

import com.example.api.model.dto.ItemResponse
import com.example.api.model.entity.Item

fun Item.toResponse() = ItemResponse(id!!, name, createdAt)

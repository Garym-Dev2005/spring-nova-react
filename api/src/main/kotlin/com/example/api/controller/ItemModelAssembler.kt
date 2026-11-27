package com.example.api.controller

import com.example.api.model.dto.ItemResponse
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.stereotype.Component

@Component
class ItemModelAssembler : RepresentationModelAssembler<ItemResponse, EntityModel<ItemResponse>> {
    override fun toModel(item: ItemResponse): EntityModel<ItemResponse> =
        EntityModel.of(
            item,
            linkTo(ItemController::class.java).slash(item.id).withSelfRel(),
            linkTo(ItemController::class.java).withRel(IanaLinkRelations.COLLECTION),
        )
}

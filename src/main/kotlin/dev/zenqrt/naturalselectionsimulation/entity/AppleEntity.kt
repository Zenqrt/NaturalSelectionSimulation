package dev.zenqrt.naturalselectionsimulation.entity

import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.item.ItemEntityMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class AppleEntity : Entity(EntityType.ITEM) {

    init {
        (entityMeta as ItemEntityMeta).item = ItemStack.of(Material.APPLE)
    }

}
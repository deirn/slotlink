@file:Suppress("UnstableApiUsage")

package badasintended.slotlink.util

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

val StorageView<ItemVariant>.isEmpty
    get() = resource.isBlank || amount == 0L

val Player.storage: PlayerInventoryStorage
    get() = PlayerInventoryStorage.of(this)

val AbstractContainerMenu.cursorStorage: SingleSlotStorage<ItemVariant>
    get() = PlayerInventoryStorage.getCursorStorage(this)

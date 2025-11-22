package badasintended.slotlink.dev

import badasintended.slotlink.block.ModBlock
import badasintended.slotlink.item.ModItem
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.core.Direction

object StorageFillerItem : Item(ModItem.SETTINGS) {

    override fun isFoil(stack: ItemStack?): Boolean {
        return true
    }

    @Suppress("UnstableApiUsage")
    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val player = context.player ?: return InteractionResult.FAIL

        if (world.isClientSide) return InteractionResult.SUCCESS

        val pos = context.clickedPos
        if (world.getBlockState(pos).block is ModBlock) return InteractionResult.SUCCESS

        val storage = ItemStorage.SIDED.find(world, pos, Direction.UP)
        if (storage != null) Transaction.openOuter().use { transaction ->
            while (true) {
                val item =
                    if (!player.offhandItem.isEmpty) player.offhandItem.item
                    else BuiltInRegistries.ITEM.getRandom(world.random).get().value()

                if (storage.insert(ItemVariant.of(item), item.maxStackSize.toLong(), transaction) == 0L) break
            }
            transaction.commit()
            player.displayClientMessage(Component.literal("Filled (${pos.x}, ${pos.y}, ${pos.z})"), true)
        }

        return InteractionResult.SUCCESS
    }

}
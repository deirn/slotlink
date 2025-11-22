package badasintended.slotlink.item

import badasintended.slotlink.util.actionBar
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

open class UnlimitedRemoteItem(id: String = "unlimited_remote") : MultiDimRemoteItem(id) {

    override val level = 2

    override fun use(
        world: Level,
        player: Player,
        stack: ItemStack,
        remoteSlot: Int,
        masterPos: BlockPos,
        masterDim: ResourceKey<Level>
    ) {
        if (world.dimension() != masterDim) {
            player.actionBar("${baseTlKey}.differentDimension")
        } else super.use(world, player, stack, remoteSlot, masterPos, masterDim)
    }

}

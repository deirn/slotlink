package badasintended.slotlink.item

import badasintended.slotlink.util.actionBar
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class LimitedRemoteItem : UnlimitedRemoteItem("limited_remote") {

    override val level = 3

    override fun use(
        world: Level,
        player: Player,
        stack: ItemStack,
        remoteSlot: Int,
        masterPos: BlockPos,
        masterDim: ResourceKey<Level>
    ) {
        if (player.position().distanceTo(Vec3.atLowerCornerOf(masterPos)) > 512) {
            player.actionBar("${baseTlKey}.tooFarFromMaster")
        } else super.use(world, player, stack, remoteSlot, masterPos, masterDim)
    }

}

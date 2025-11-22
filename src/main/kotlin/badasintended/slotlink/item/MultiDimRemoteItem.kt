package badasintended.slotlink.item

import badasintended.slotlink.network.Network
import badasintended.slotlink.util.actionBar
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

open class MultiDimRemoteItem(id: String = "multi_dim_remote") : RemoteItem(id) {

    override val level = 1

    override fun use(
        world: Level,
        player: Player,
        stack: ItemStack,
        remoteSlot: Int,
        masterPos: BlockPos,
        masterDim: ResourceKey<Level>
    ) {
        if (!world.isClientSide) {
            val dim = world.server!!.getLevel(masterDim)
            if (dim == null) {
                player.actionBar("${baseTlKey}.invalidDim")
            } else {
                val network = Network.get(dim, masterPos)
                if (network == null || network.deleted) {
                    player.actionBar("${baseTlKey}.masterNotFound")
                } else {
                    network.master?.also {
                        player.openMenu(ScreenHandlerFactory(dim, it, remoteSlot))
                    }
                }
            }
        }
    }

}

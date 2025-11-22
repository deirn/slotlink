package badasintended.slotlink.block.entity

import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.screen.RequestScreenHandler
import badasintended.slotlink.storage.FilterFlags
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.MenuProvider
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos

class RequestBlockEntity(pos: BlockPos, state: BlockState) :
    ChildBlockEntity(BlockEntityTypes.REQUEST, NodeType.REQUEST, pos, state),
    MenuProvider {

    val watchers = ObjectOpenHashSet<BlockEntityWatcher<RequestBlockEntity>>()

    override fun setRemoved() {
        super.setRemoved()
        watchers.forEach { it.onRemoved() }
    }

    override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu? {
        val world = level ?: return null
        network?.also { network ->
            val master = network.master ?: return null
            val storages = master.getStorages(world, FilterFlags.INSERT, true)
            val handler = RequestScreenHandler(syncId, inv, storages, this, master)
            watchers.add(handler)
            master.watchers.add(handler)
            master.markForcedChunks()
            return handler
        }
        return null
    }

    override fun getDisplayName() = Component.translatable("container.slotlink.request")!!

}

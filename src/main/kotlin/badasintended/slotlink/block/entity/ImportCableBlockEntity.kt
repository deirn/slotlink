package badasintended.slotlink.block.entity

import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.init.Blocks
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.storage.FilterFlags
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

@Suppress("UnstableApiUsage")
class ImportCableBlockEntity(pos: BlockPos, state: BlockState) :
    TransferCableBlockEntity(Blocks.IMPORT_CABLE, BlockEntityTypes.IMPORT_CABLE, NodeType.IMPORT, pos, state) {

    override var side = Direction.DOWN

    override fun getSource(world: Level, master: MasterBlockEntity): Storage<ItemVariant> {
        return getStorage(world, side, FilterFlags.EXTRACT)
    }

    override fun getTarget(world: Level, master: MasterBlockEntity): Storage<ItemVariant> {
        return master.getStorages(world, FilterFlags.INSERT)
    }

}

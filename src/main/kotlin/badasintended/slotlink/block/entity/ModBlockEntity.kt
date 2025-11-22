package badasintended.slotlink.block.entity

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

abstract class ModBlockEntity(type: BlockEntityType<out BlockEntity>, pos: BlockPos, state: BlockState) :
    BlockEntity(type, pos, state) {

    override fun setChanged() {
        super.setChanged()

        val world = level as? ServerLevel ?: return
        world.chunkSource.blockChanged(worldPosition)
    }

}
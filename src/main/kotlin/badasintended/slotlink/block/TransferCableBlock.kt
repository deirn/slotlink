package badasintended.slotlink.block

import badasintended.slotlink.util.BlockEntityBuilder
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.LeverBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor

abstract class TransferCableBlock(id: String, builder: BlockEntityBuilder) : ConnectorCableBlock(id, builder) {

    override fun isIgnored(blockState: BlockState) = false

    override fun connect(
        state: BlockState,
        direction: Direction,
        world: LevelAccessor,
        neighborState: BlockState,
        neighborPos: BlockPos
    ): BlockState {
        val fromSuper = super.connect(state, direction, world, neighborState, neighborPos)
        val block = neighborState.block
        return fromSuper.setValue(PROPERTIES[direction], block is ModBlock || block is LeverBlock)
    }

}

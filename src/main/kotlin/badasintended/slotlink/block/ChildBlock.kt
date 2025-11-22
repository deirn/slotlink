package badasintended.slotlink.block

import badasintended.slotlink.block.entity.ChildBlockEntity
import badasintended.slotlink.network.Node
import badasintended.slotlink.util.BlockEntityBuilder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

abstract class ChildBlock(
    id: String,
    private val blockEntityBuilder: BlockEntityBuilder,
    settings: Properties = SETTINGS
) : ModBlock(id, settings) {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun neighborChanged(
        selfState: BlockState,
        world: Level,
        pos: BlockPos,
        block: Block,
        neighborPos: BlockPos,
        moved: Boolean
    ) {
        if (world.isClientSide) return

        val node = world.getBlockEntity(pos) as Node
        val neighborNode = world.getBlockEntity(neighborPos) as? Node
        val neighborState = world.getBlockState(neighborPos)

        if (node.connect(neighborNode)) {
            world.blockUpdated(pos, block)
        } else {
            neighborNode?.also {
                if (it.connect(node)) {
                    world.blockUpdated(neighborPos, neighborState.block)
                }
            }
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (neighborState.isAir) {
            val node = world.getBlockEntity(pos) as Node
            node.connection.sides.remove(direction)
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = blockEntityBuilder(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is ChildBlockEntity) {
                blockEntity.disconnect()
            }
            super.onRemove(state, world, pos, newState, moved)
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        world: BlockGetter?,
        tooltip: MutableList<Component>,
        options: TooltipFlag
    ) {
        super.appendHoverText(stack, world, tooltip, options)
        tooltip.add(Component.translatable("block.slotlink.child.tooltip").withStyle(ChatFormatting.GRAY))
    }

}

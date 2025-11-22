package badasintended.slotlink.block

import badasintended.slotlink.property.NullableProperty
import badasintended.slotlink.property.getNull
import badasintended.slotlink.property.with
import badasintended.slotlink.util.BlockEntityBuilder
import badasintended.slotlink.util.bbCuboid
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

abstract class ConnectorCableBlock(id: String, builder: BlockEntityBuilder) : CableBlock(id, builder) {

    companion object {

        val CONNECTED = NullableProperty(DirectionProperty.create("connected"))

        val endShape = bbCuboid(5, 5, 5, 6, 6, 6)
        val connectionShapes = mapOf(
            null to Shapes.empty(),
            Direction.NORTH to bbCuboid(5, 5, 0, 6, 6, 2),
            Direction.SOUTH to bbCuboid(5, 5, 14, 6, 6, 2),
            Direction.EAST to bbCuboid(14, 5, 5, 2, 6, 6),
            Direction.WEST to bbCuboid(0, 5, 5, 2, 6, 6),
            Direction.UP to bbCuboid(5, 14, 5, 6, 2, 6),
            Direction.DOWN to bbCuboid(5, 0, 5, 6, 2, 6)
        )

    }

    init {
        registerDefaultState(defaultBlockState().with(CONNECTED, null))
    }

    @Suppress("UnstableApiUsage")
    private fun checkLink(
        state: BlockState,
        direction: Direction,
        world: LevelAccessor,
        neighborState: BlockState,
        neighborPos: BlockPos
    ): BlockState {
        if (world !is ServerLevel) return state

        var result = state
        val connected = state.getNull(CONNECTED)
        if (connected == direction && neighborState.isAir) {
            result = result.with(CONNECTED, null)
        } else if (connected == null && !isIgnored(neighborState)) {
            for (d in UPDATE_SHAPE_ORDER) {
                val storage = ItemStorage.SIDED.find(world, neighborPos, d)
                if (storage != null) {
                    result = result
                        .with(CONNECTED, direction)
                        .setValue(PROPERTIES[direction], true)
                    break
                }
            }
        }
        return result
    }

    abstract fun isIgnored(blockState: BlockState): Boolean

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CONNECTED)
    }

    override fun connect(
        state: BlockState,
        direction: Direction,
        world: LevelAccessor,
        neighborState: BlockState,
        neighborPos: BlockPos
    ): BlockState {
        val fromSuper = super.connect(state, direction, world, neighborState, neighborPos)
        return checkLink(fromSuper, direction, world, neighborState, neighborPos)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var state = super.getStateForPlacement(ctx) ?: return null
        val connected = state.getNull(CONNECTED)
        state = state.with(CONNECTED, null)
        if (connected != null) {
            state = state.setValue(PROPERTIES[connected], false)
        }

        val world = ctx.level
        val opposite = ctx.clickedFace.opposite
        val oppositePos = ctx.clickedPos.relative(opposite)
        val oppositeState = world.getBlockState(oppositePos)

        state = checkLink(state, opposite, world, oppositeState, oppositePos)
        if (state.getNull(CONNECTED) == null) {
            state = state.with(CONNECTED, connected)
            if (connected != null) {
                state = state.setValue(PROPERTIES[connected], true)
            }
        }

        return state
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
        var updatedState = super.updateShape(state, direction, neighborState, world, pos, neighborPos)
        if (updatedState.getNull(CONNECTED) == null) {
            UPDATE_SHAPE_ORDER.forEach {
                val offset = pos.relative(it)
                updatedState = connect(updatedState, it, world, world.getBlockState(offset), offset)
            }
        }
        updatedState.getNull(CONNECTED)?.also {
            updatedState = updatedState.setValue(PROPERTIES[it], true)
        }
        return updatedState
    }

    override fun appendHoverText(
        stack: ItemStack,
        world: BlockGetter?,
        tooltip: MutableList<Component>,
        options: TooltipFlag
    ) {
        super.appendHoverText(stack, world, tooltip, options)
        tooltip.add(Component.translatable("block.slotlink.filter.tooltip").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("block.slotlink.connector_cable.tooltip").withStyle(ChatFormatting.GRAY))
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (player.mainHandItem.isEmpty) {
            player.openMenu(state.getMenuProvider(world, pos))
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, view: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape {
        var key = 1
        sideShapes.keys.forEach { key = (key shl 1) + if (state.getValue(it)) 1 else 0 }
        key = key shl connectionShapes.size
        val connected = state.getValue(CONNECTED).value
        connected?.also { key += it.get3DDataValue() + 1 }
        return voxelCache.getOrPut(key) {
            Shapes.or(
                endShape,
                connectionShapes[connected],
                *sideShapes.filter { state.getValue(it.key) }.values.toTypedArray()
            )
        }
    }

}

package badasintended.slotlink.block

import badasintended.slotlink.block.entity.CableBlockEntity
import badasintended.slotlink.util.BlockEntityBuilder
import badasintended.slotlink.util.bbCuboid
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN
import net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST
import net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH
import net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH
import net.minecraft.world.level.block.state.properties.BlockStateProperties.UP
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor

open class CableBlock(id: String = "cable", be: BlockEntityBuilder = ::CableBlockEntity) :
    ChildBlock(id, be, SETTINGS) {

    companion object {

        val SETTINGS: Properties = FabricBlockSettings
            .of(Material.GLASS)
            .destroyTime(3f)

        val PROPERTIES = mapOf(
            Direction.NORTH to NORTH,
            Direction.SOUTH to SOUTH,
            Direction.EAST to EAST,
            Direction.WEST to WEST,
            Direction.UP to UP,
            Direction.DOWN to DOWN
        )

        val centerShape = bbCuboid(6, 6, 6, 4, 4, 4)

        val sideShapes = mapOf(
            NORTH to bbCuboid(6, 6, 0, 4, 4, 10),
            SOUTH to bbCuboid(6, 6, 6, 4, 4, 10),
            EAST to bbCuboid(6, 6, 6, 10, 4, 4),
            WEST to bbCuboid(0, 6, 6, 10, 4, 4),
            UP to bbCuboid(6, 6, 6, 4, 10, 4),
            DOWN to bbCuboid(6, 0, 6, 4, 10, 4)
        )

        val voxelCache = Int2ObjectOpenHashMap<VoxelShape>()

    }

    init {
        for (property in PROPERTIES.values) {
            registerDefaultState(defaultBlockState().setValue(property, false))
        }
    }

    protected open fun connect(
        state: BlockState,
        direction: Direction,
        world: LevelAccessor,
        neighborState: BlockState,
        neighborPos: BlockPos
    ): BlockState {
        val block = neighborState.block
        return state.setValue(PROPERTIES[direction], block is ModBlock)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        val world = ctx.level
        val pos = ctx.clickedPos

        var state = defaultBlockState()
        for (direction in UPDATE_SHAPE_ORDER) {
            val offset = pos.relative(direction)
            state = connect(state, direction, world, world.getBlockState(offset), offset)
        }
        return state
    }

    @Suppress("DEPRECATION")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return connect(
            super.updateShape(state, direction, neighborState, world, pos, neighborPos),
            direction,
            world,
            neighborState,
            neighborPos
        )
    }

    override fun getShape(state: BlockState, view: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape {
        var key = 0
        sideShapes.keys.forEach { key = (key shl 1) + if (state.getValue(it)) 1 else 0 }
        return voxelCache.getOrPut(key) {
            Shapes.or(centerShape, *sideShapes.filter { state.getValue(it.key) }.values.toTypedArray())
        }
    }

}

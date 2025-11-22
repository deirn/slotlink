package badasintended.slotlink.block.entity

import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.network.NodeType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos

class CableBlockEntity(pos: BlockPos, state: BlockState) :
    ChildBlockEntity(BlockEntityTypes.CABLE, NodeType.CABLE, pos, state)

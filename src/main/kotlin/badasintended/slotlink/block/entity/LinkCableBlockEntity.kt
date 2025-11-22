package badasintended.slotlink.block.entity

import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.init.Blocks
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.screen.ConnectorCableScreenHandler
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.core.BlockPos

class LinkCableBlockEntity(pos: BlockPos, state: BlockState) :
    ConnectorCableBlockEntity(Blocks.LINK_CABLE, BlockEntityTypes.LINK_CABLE, NodeType.LINK, pos, state) {

    override fun createMenu(syncId: Int, inv: Inventory, player: Player) = ConnectorCableScreenHandler(
        syncId, inv, blacklist, filter, priority, ContainerLevelAccess.create(level, worldPosition)
    )

}

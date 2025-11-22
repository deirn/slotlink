package badasintended.slotlink.block

import badasintended.slotlink.block.entity.LinkCableBlockEntity
import badasintended.slotlink.util.ignoredTag
import net.minecraft.world.level.block.state.BlockState

class LinkCableBlock : ConnectorCableBlock("link_cable", ::LinkCableBlockEntity) {

    override fun isIgnored(blockState: BlockState): Boolean {
        return blockState.`is`(ignoredTag)
    }

}

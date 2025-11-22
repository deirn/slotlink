package badasintended.slotlink.block.entity

import net.minecraft.world.level.block.entity.BlockEntity

interface BlockEntityWatcher<T : BlockEntity> {

    fun onRemoved()

}
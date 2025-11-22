package badasintended.slotlink.block

import badasintended.slotlink.util.modId
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.material.Material

abstract class ModBlock(id: String, settings: Properties = SETTINGS) : BaseEntityBlock(settings) {

    companion object {

        val SETTINGS: Properties = FabricBlockSettings
            .of(Material.STONE)
            .destroyTime(5f)

    }

    val id = modId(id)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getRenderShape(state: BlockState?): RenderShape {
        return RenderShape.MODEL
    }

}

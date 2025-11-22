package badasintended.slotlink.block

import badasintended.slotlink.block.entity.RequestBlockEntity
import badasintended.slotlink.util.actionBar
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.MenuProvider
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class RequestBlock : ChildBlock("request", ::RequestBlockEntity) {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (!world.isClientSide) {
            val request = world.getBlockEntity(pos) as RequestBlockEntity
            request.network.also {
                if (it == null || it.deleted) {
                    player.actionBar("${descriptionId}.hasNoMaster")
                } else {
                    player.openMenu(state.getMenuProvider(world, pos))
                }
            }
        }
        return InteractionResult.SUCCESS
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getMenuProvider(
        state: BlockState,
        world: Level,
        pos: BlockPos
    ): MenuProvider? {
        val blockEntity = world.getBlockEntity(pos) ?: return null
        if (blockEntity !is RequestBlockEntity) return null
        return blockEntity
    }

}

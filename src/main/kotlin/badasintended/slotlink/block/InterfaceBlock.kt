package badasintended.slotlink.block

import badasintended.slotlink.block.entity.InterfaceBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

class InterfaceBlock : ChildBlock("interface", ::InterfaceBlockEntity) {

    override fun appendHoverText(
        stack: ItemStack,
        world: BlockGetter?,
        tooltip: MutableList<Component>,
        options: TooltipFlag
    ) {
        super.appendHoverText(stack, world, tooltip, options)
        tooltip.add(Component.translatable("block.slotlink.filter.tooltip").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("block.slotlink.interface.tooltip").withStyle(ChatFormatting.GRAY))
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

}
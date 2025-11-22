package badasintended.slotlink.block

import badasintended.slotlink.block.entity.MasterBlockEntity
import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.network.Network
import badasintended.slotlink.network.Node
import badasintended.slotlink.util.actionBar
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.ListTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

class MasterBlock : ModBlock("master"), BlockAttackAware {

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = MasterBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, BlockEntityTypes.MASTER, MasterBlockEntity.Ticker)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack)

        val blockEntity = world.getBlockEntity(pos)!!
        val nbt = blockEntity.saveWithoutMetadata()

        nbt.put("storagePos", ListTag())
        blockEntity.load(nbt)
        blockEntity.setChanged()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun neighborChanged(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        block: Block,
        neighborPos: BlockPos,
        moved: Boolean
    ) {
        val neighborState = world.getBlockState(neighborPos)
        val neighborBlock = neighborState.block

        if (neighborBlock is ChildBlock) {
            val node = world.getBlockEntity(neighborPos) as? Node
            node?.also {
                val master = world.getBlockEntity(pos) as MasterBlockEntity
                if (it.connect(master)) {
                    world.blockUpdated(neighborPos, neighborBlock)
                }
            }
        }
    }

    override fun destroy(world: LevelAccessor, pos: BlockPos, state: BlockState) {
        super.destroy(world, pos, state)

        if (world is Level) {
            Network.get(world, pos)?.delete()
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        world: BlockGetter?,
        tooltip: MutableList<Component>,
        options: TooltipFlag
    ) {
        super.appendHoverText(stack, world, tooltip, options)
        tooltip.add(Component.translatable("block.slotlink.master.tooltip").withStyle(ChatFormatting.GRAY))
    }

    override fun onBlockAttack(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        direction: Direction
    ): InteractionResult {
        if (!player.isSpectator && player.isShiftKeyDown && player.getItemInHand(hand).isEmpty) {
            Network.get(world, pos)?.validate()
            if (!player.isCreative) player.playSound(SoundEvents.STONE_BREAK, 1.0f, 1.0f)
            player.actionBar("block.slotlink.master.revalidated")
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

}

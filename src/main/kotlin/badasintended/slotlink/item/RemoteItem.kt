package badasintended.slotlink.item

import badasintended.slotlink.block.entity.MasterBlockEntity
import badasintended.slotlink.init.Blocks
import badasintended.slotlink.screen.RemoteScreenHandler
import badasintended.slotlink.storage.FilterFlags
import badasintended.slotlink.util.actionBar
import badasintended.slotlink.util.int
import badasintended.slotlink.util.toArray
import badasintended.slotlink.util.toPos
import it.unimi.dsi.fastutil.ints.IntSet
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResultHolder
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

abstract class RemoteItem(id: String) : ModItem(id, SETTINGS.stacksTo(1)) {

    abstract val level: Int

    protected val baseTlKey = "item.slotlink.remote"

    fun use(world: Level, player: Player, stack: ItemStack, remoteSlot: Int) {
        val network = stack.getTagElement("network")

        if (network == null) {
            player.actionBar("${baseTlKey}.hasNoMaster")
        } else {
            val pos = network.getIntArray("pos").toPos()
            val dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation(network.getString("dim")))
            use(world, player, stack, remoteSlot, pos, dim)
        }
    }

    protected abstract fun use(
        world: Level,
        player: Player,
        stack: ItemStack,
        remoteSlot: Int,
        masterPos: BlockPos,
        masterDim: ResourceKey<Level>
    )

    override fun use(world: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = when (hand) {
            InteractionHand.MAIN_HAND -> player.mainHandItem
            InteractionHand.OFF_HAND -> player.offhandItem
        }

        val slot = when (hand) {
            InteractionHand.MAIN_HAND -> player.inventory.selected
            InteractionHand.OFF_HAND -> Inventory.SLOT_OFFHAND
        }

        use(world, player, stack, slot)
        return InteractionResultHolder.success(stack)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val stack = context.itemInHand
        val world = context.level
        val pos = context.clickedPos
        val block = world.getBlockState(pos).block

        if (block == Blocks.MASTER && player != null && player.isShiftKeyDown) {
            val dimId = world.dimension().location().toString()
            val tag = stack.getOrCreateTagElement("network")
            tag.putIntArray("pos", pos.toArray())
            tag.putString("dim", dimId)
            player.actionBar("${baseTlKey}.linked", pos.x, pos.y, pos.z, dimId)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }

    override fun appendHoverText(stack: ItemStack, world: Level?, tooltip: MutableList<Component>, context: TooltipFlag) {
        super.appendHoverText(stack, world, tooltip, context)

        tooltip.add(Component.translatable("${baseTlKey}.useTooltip").withStyle(ChatFormatting.GRAY))

        val tag = stack.orCreateTag
        if (tag.contains("network")) {
            val network = tag.getCompound("network")
            val pos = network.getIntArray("pos")
            val dim = network.getString("dim")
            tooltip.add(
                Component.translatable("${baseTlKey}.info", pos[0], pos[1], pos[2], dim).withStyle(
                    ChatFormatting.DARK_PURPLE
                )
            )
        }
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (entity is Holder) {
            entity.possibleRemoteSlots.add(slot)
        }
    }

    interface Holder {

        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("slotlink\$getPossibleRemoteSlots")
        val possibleRemoteSlots: IntSet

    }

    class ScreenHandlerFactory(
        masterWorld: Level,
        private val master: MasterBlockEntity,
        private val remoteSlot: Int
    ) : ExtendedScreenHandlerFactory {

        private val storages = master.getStorages(masterWorld, FilterFlags.INSERT, true)

        override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu {
            val handler = RemoteScreenHandler(syncId, inv, storages, master, remoteSlot)
            master.watchers.add(handler)
            master.markForcedChunks()
            return handler
        }

        override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
            buf.int(remoteSlot)
        }

        override fun getDisplayName() = Component.translatable("container.slotlink.request")!!

    }

}
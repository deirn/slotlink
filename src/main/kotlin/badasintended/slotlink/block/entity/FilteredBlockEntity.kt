package badasintended.slotlink.block.entity

import badasintended.slotlink.network.NodeType
import badasintended.slotlink.util.ObjBoolPair
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.to
import badasintended.slotlink.util.writeFilter
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos

abstract class FilteredBlockEntity(
    blockEntityType: BlockEntityType<out BlockEntity>,
    nodeType: NodeType<*>,
    pos: BlockPos,
    state: BlockState
) : ChildBlockEntity(blockEntityType, nodeType, pos, state),
    ExtendedScreenHandlerFactory {

    var blacklist = false
    var filter: NonNullList<ObjBoolPair<ItemStack>> = NonNullList.withSize(9, ItemStack.EMPTY to false)

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)

        nbt.putBoolean("isBlacklist", blacklist)

        val filterTag = CompoundTag()
        val list = ListTag()
        filter.forEachIndexed { i, pair ->
            if (!pair.first.isEmpty) {
                val compound = CompoundTag()
                compound.putByte("Slot", i.toByte())
                compound.putBoolean("matchNbt", pair.second)
                pair.first.save(compound)
                list.add(compound)
            }
        }
        filterTag.put("Items", list)
        nbt.put("filter", filterTag)
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)

        blacklist = nbt.getBoolean("isBlacklist")
        val filterTag = nbt.getCompound("filter")
        val list = filterTag.getList("Items", Tag.TAG_COMPOUND.toInt())

        list.forEach {
            it as CompoundTag
            val slot = it.getByte("Slot").toInt()
            val matchNbt = it.getBoolean("matchNbt")
            if (slot in 0 until 9) {
                val stack = ItemStack.of(it)
                filter[slot] = stack to matchNbt
            }
        }
    }

    override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
        buf.apply {
            bool(blacklist)
            writeFilter(filter)
        }
    }

    override fun getDisplayName() = Component.translatable("container.slotlink.filter", worldPosition.x, worldPosition.y, worldPosition.z)!!

}
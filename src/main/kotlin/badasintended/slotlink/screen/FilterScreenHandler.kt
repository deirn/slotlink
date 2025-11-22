package badasintended.slotlink.screen

import badasintended.slotlink.block.entity.FilteredBlockEntity
import badasintended.slotlink.init.Screens
import badasintended.slotlink.util.ObjBoolPair
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.readFilter
import badasintended.slotlink.util.to
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot

@Suppress("LeakingThis")
open class FilterScreenHandler(
    syncId: Int,
    playerInv: Inventory,
    var blacklist: Boolean,
    val filter: MutableList<ObjBoolPair<ItemStack>>,
    private val context: ContainerLevelAccess
) : AbstractContainerMenu(null, syncId) {

    constructor(syncId: Int, playerInv: Inventory, buf: FriendlyByteBuf) : this(
        syncId, playerInv, buf.bool, buf.readFilter(), ContainerLevelAccess.NULL
    )

    init {
        for (m in 0 until 3) for (l in 0 until 9) {
            addSlot(Slot(playerInv, l + m * 9 + 9, 8 + l * 18, 84 + m * 18))
        }
        for (m in 0 until 9) {
            addSlot(Slot(playerInv, m, 8 + m * 18, 142))
        }
    }

    fun filterSlotClick(i: Int, filterStack: ItemStack, matchNbt: Boolean) {
        val stack = filterStack.copy().apply { count = 1 }
        filter[i] = stack to (matchNbt && !filterStack.isEmpty)
    }

    open fun onClose(blockEntity: FilteredBlockEntity) {
        blockEntity.blacklist = blacklist
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()
            val s = if (index < 27) 27 else 0
            val e = if (index < 27) 36 else 27
            if (!moveItemStackTo(itemStack2, s, e, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTake(player, itemStack2)
        }
        return itemStack
    }

    override fun removed(player: Player?) {
        super.removed(player)
        context.execute { world, pos ->
            val be = world.getBlockEntity(pos)
            if (be is FilteredBlockEntity) {
                onClose(be)
                be.setChanged()
            }
        }
    }

    override fun stillValid(player: Player) = true

    override fun getType(): MenuType<*> = Screens.FILTER

}
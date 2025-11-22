package badasintended.slotlink.screen

import badasintended.slotlink.block.entity.FilteredBlockEntity
import badasintended.slotlink.block.entity.TransferCableBlockEntity
import badasintended.slotlink.block.entity.TransferCableBlockEntity.Mode
import badasintended.slotlink.init.Screens
import badasintended.slotlink.util.ObjBoolPair
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.int
import badasintended.slotlink.util.readFilter
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType
import net.minecraft.core.Direction

class TransferCableScreenHandler(
    syncId: Int,
    playerInv: Inventory,
    blacklist: Boolean,
    filter: MutableList<ObjBoolPair<ItemStack>>,
    priority: Int,
    var side: Direction,
    var mode: Mode,
    context: ContainerLevelAccess
) : ConnectorCableScreenHandler(syncId, playerInv, blacklist, filter, priority, context) {

    constructor(syncId: Int, playerInv: Inventory, buf: FriendlyByteBuf) : this(
        syncId,
        playerInv,
        buf.bool,
        buf.readFilter(),
        buf.int,
        Direction.from3DDataValue(buf.int),
        Mode.of(buf.int),
        ContainerLevelAccess.NULL
    )

    override fun onClose(blockEntity: FilteredBlockEntity) {
        super.onClose(blockEntity)
        if (blockEntity is TransferCableBlockEntity) {
            blockEntity.side = side
            blockEntity.mode = mode
        }
    }

    override fun getType(): MenuType<*> = Screens.TRANSFER_CABLE

}

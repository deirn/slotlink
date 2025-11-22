package badasintended.slotlink.screen

import badasintended.slotlink.block.entity.ConnectorCableBlockEntity
import badasintended.slotlink.block.entity.FilteredBlockEntity
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

open class ConnectorCableScreenHandler(
    syncId: Int,
    playerInv: Inventory,
    blacklist: Boolean,
    filter: MutableList<ObjBoolPair<ItemStack>>,
    var priority: Int,
    context: ContainerLevelAccess
) : FilterScreenHandler(syncId, playerInv, blacklist, filter, context) {

    constructor(syncId: Int, playerInv: Inventory, buf: FriendlyByteBuf) : this(
        syncId, playerInv, buf.bool, buf.readFilter(), buf.int, ContainerLevelAccess.NULL
    )

    override fun onClose(blockEntity: FilteredBlockEntity) {
        super.onClose(blockEntity)
        if (blockEntity is ConnectorCableBlockEntity) {
            blockEntity.priority = priority
        }
    }

    override fun getType(): MenuType<*> = Screens.CONNECTOR_CABLE

}

package badasintended.slotlink.screen

import badasintended.slotlink.block.entity.MasterBlockEntity
import badasintended.slotlink.init.Screens
import badasintended.slotlink.screen.slot.LockedSlot
import badasintended.slotlink.storage.NetworkStorage
import badasintended.slotlink.util.int
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.MenuType

class RemoteScreenHandler : RequestScreenHandler {

    private val remoteSlot: Int

    constructor(
        syncId: Int,
        playerInventory: Inventory,
        storage: NetworkStorage,
        master: MasterBlockEntity,
        remoteSlot: Int
    ) : super(syncId, playerInventory, storage, null, master) {
        this.remoteSlot = remoteSlot
    }

    constructor(syncId: Int, playerInventory: Inventory, buf: FriendlyByteBuf) : super(syncId, playerInventory) {
        this.remoteSlot = buf.int
    }

    override fun resize(viewedHeight: Int, craft: Boolean) {
        super.resize(viewedHeight, craft)

        if (remoteSlot >= 0) {
            val remote = playerInventory.getItem(remoteSlot)
            playerInventory.apply {
                slots.forEachIndexed { i, slot ->
                    if (slot.item == remote) slots[i] = LockedSlot(slot.container, slot.containerSlot, slot.x, slot.y)
                }
            }
        }
    }

    override fun getType(): MenuType<*> = Screens.REMOTE

}

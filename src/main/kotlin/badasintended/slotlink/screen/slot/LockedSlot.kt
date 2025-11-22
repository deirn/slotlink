package badasintended.slotlink.screen.slot

import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.Slot

class LockedSlot(inventory: Container, index: Int, x: Int = Int.MIN_VALUE, y: Int = Int.MIN_VALUE) :
    Slot(inventory, index, x, y) {

    override fun mayPlace(stack: ItemStack) = false
    override fun mayPickup(playerEntity: Player) = false

}
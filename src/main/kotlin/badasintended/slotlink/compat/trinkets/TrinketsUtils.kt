package badasintended.slotlink.compat.trinkets

import dev.emi.trinkets.api.SlotReference
import net.minecraft.world.item.ItemStack

internal var SlotReference.stack: ItemStack
    get() = inventory.getItem(index)
    set(value) = inventory.setItem(index, value)
package badasintended.slotlink.dev

import badasintended.slotlink.util.modId
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry

@Suppress("unused")
object SlotlinkDev {

    fun main() {
        Registry.register(BuiltInRegistries.ITEM, modId("storage_filler"), StorageFillerItem)
    }

}
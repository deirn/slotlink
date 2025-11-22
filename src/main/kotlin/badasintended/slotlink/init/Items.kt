package badasintended.slotlink.init

import badasintended.slotlink.item.LimitedRemoteItem
import badasintended.slotlink.item.ModItem
import badasintended.slotlink.item.MultiDimRemoteItem
import badasintended.slotlink.item.UnlimitedRemoteItem
import badasintended.slotlink.util.modId
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries.ITEM
import net.minecraft.core.Registry

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Items : Initializer {

    val GROUP = FabricItemGroup.builder(modId("group"))
        .icon { ItemStack(Blocks.MASTER) }
        .displayItems { _, entries ->
            Blocks.BLOCKS.forEach { entries.accept(ItemStack(it)) }
            ITEMS.forEach { entries.accept(ItemStack(it)) }
        }
        .build()!!

    val ITEMS = arrayListOf<ModItem>()

    val MULTI_DIM_REMOTE = MultiDimRemoteItem()
    val UNLIMITED_REMOTE = UnlimitedRemoteItem()
    val LIMITED_REMOTE = LimitedRemoteItem()

    override fun main() {
        r(MULTI_DIM_REMOTE, UNLIMITED_REMOTE, LIMITED_REMOTE)
    }

    private fun r(vararg items: ModItem) {
        items.forEach {
            Registry.register(ITEM, it.id, it)
            ITEMS.add(it)
        }
    }

}

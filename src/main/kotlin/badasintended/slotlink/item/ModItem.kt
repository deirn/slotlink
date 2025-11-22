package badasintended.slotlink.item

import badasintended.slotlink.util.modId
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.world.level.Level

abstract class ModItem(id: String, settings: Properties = SETTINGS) : Item(settings) {

    val id = modId(id)

    companion object {

        val SETTINGS: Properties get() = Properties()

    }

    override fun appendHoverText(stack: ItemStack, world: Level?, tooltip: MutableList<Component>, context: TooltipFlag) {
        tooltip.add(Component.translatable("${descriptionId}.tooltip").withStyle(ChatFormatting.GRAY))
    }

}

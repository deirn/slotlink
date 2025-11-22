package badasintended.slotlink.client.gui.screen

import badasintended.slotlink.client.gui.widget.ButtonWidget
import badasintended.slotlink.client.gui.widget.FilterSlotWidget
import badasintended.slotlink.client.util.GuiTextures
import badasintended.slotlink.client.util.bind
import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.init.Packets.FILTER_SETTINGS
import badasintended.slotlink.screen.FilterScreenHandler
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.int
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
open class FilterScreen<H : FilterScreenHandler>(h: H, inventory: Inventory, title: Component) :
    ModScreen<H>(h, inventory, title) {

    val filterSlots = mutableListOf<FilterSlotWidget>()

    private var blacklist = menu.blacklist

    override val baseTlKey: String
        get() = "container.slotlink.filter"

    override fun init() {
        super.init()

        titleLabelX = (imageWidth - font.width(title)) / 2
        val x = leftPos + 7
        val y = topPos + titleLabelY + 11

        val filterSlotX = x + (3 * 18)

        filterSlots.clear()
        for (i in 0 until 9) {
            filterSlots += add(FilterSlotWidget(menu, i, filterSlotX + (i % 3) * 18, y + (i / 3) * 18))
        }

        add(ButtonWidget(x + 6 * 18 + 4, y + 20, 14, 14)) {
            bgU = 228
            bgV = 28
            u = { 228 }
            v = { if (blacklist) 14 else 0 }
            tooltip = { tl("blacklist.$blacklist") }
            onPressed = {
                blacklist = !blacklist
                sync()
            }
        }
    }

    override fun renderBg(matrices: PoseStack, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(matrices, delta, mouseX, mouseY)

        GuiTextures.FILTER.bind()
        blit(matrices, leftPos, topPos, 0, 0, 176, 166)
    }

    protected open fun sync() {
        c2s(FILTER_SETTINGS) {
            int(menu.containerId)
            bool(blacklist)
        }
    }

}
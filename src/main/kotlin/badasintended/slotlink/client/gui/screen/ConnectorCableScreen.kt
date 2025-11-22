package badasintended.slotlink.client.gui.screen

import badasintended.slotlink.client.gui.widget.ButtonWidget
import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.init.Packets.PRIORITY_SETTINGS
import badasintended.slotlink.screen.ConnectorCableScreenHandler
import badasintended.slotlink.util.int
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
open class ConnectorCableScreen<H : ConnectorCableScreenHandler>(h: H, inventory: Inventory, title: Component) :
    FilterScreen<H>(h, inventory, title) {

    private var priority = menu.priority
    private var blacklist = menu.blacklist

    override fun init() {
        super.init()

        val x = leftPos + 7
        val y = topPos + titleLabelY + 11

        add(ButtonWidget(x + 2 * 18, y + 2, 14, 14)) {
            bgU = 228
            bgV = 28
            u = { 242 }
            v = { 0 }
            onPressed = {
                priority++
                sync()
            }
        }

        add(ButtonWidget(x + 2 * 18, y + 2 + 2 * 18, 14, 14)) {
            bgU = 228
            bgV = 28
            u = { 242 }
            v = { 14 }
            onPressed = {
                priority--
                sync()
            }
        }
    }

    override fun sync() {
        super.sync()
        c2s(PRIORITY_SETTINGS) {
            int(menu.containerId)
            int(priority)
        }
    }

    override fun renderLabels(matrices: PoseStack, mouseX: Int, mouseY: Int) {
        super.renderLabels(matrices, mouseX, mouseY)

        font.draw(matrices, "$priority", 7 + 2 * 18f, titleLabelY + 31f, 4210752)
    }

}

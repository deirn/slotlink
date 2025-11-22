package badasintended.slotlink.client.gui.screen

import badasintended.slotlink.client.gui.widget.ButtonWidget
import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.init.Packets
import badasintended.slotlink.screen.TransferCableScreenHandler
import badasintended.slotlink.util.int
import badasintended.slotlink.util.next
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class TransferCableScreen(h: TransferCableScreenHandler, inventory: Inventory, title: Component) :
    ConnectorCableScreen<TransferCableScreenHandler>(h, inventory, title) {

    private var side = menu.side
    private var redstone = menu.mode

    override fun init() {
        super.init()

        val x = leftPos + 7
        val y = topPos + titleLabelY + 11

        add(ButtonWidget(x + 6 * 18 + 4, y + 2, 14, 14)) {
            bgU = 228
            bgV = 28
            u = { 214 }
            v = { redstone.ordinal * 14 }
            tooltip = { tl("redstone.$redstone") }
            onPressed = {
                redstone = redstone.next()
                sync()
            }
        }

        add(ButtonWidget(x + 6 * 18 + 4, y + 38, 14, 14)) {
            bgU = 228
            bgV = 28
            u = { 200 }
            v = { side.ordinal * 14 }
            tooltip = { tl("side.$side") }
            onPressed = {
                side = side.next()
                sync()
            }
        }
    }

    override fun sync() {
        super.sync()
        c2s(Packets.TRANSFER_SETTINGS) {
            int(menu.containerId)
            int(redstone.ordinal)
            int(side.get3DDataValue())
        }
    }

}

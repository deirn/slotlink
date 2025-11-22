package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.init.Packets
import badasintended.slotlink.screen.RequestScreenHandler
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.int
import badasintended.slotlink.util.result
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screens.Screen

@Environment(EnvType.CLIENT)
class CraftingResultSlotWidget(handler: RequestScreenHandler, x: Int, y: Int) :
    SlotWidget<RequestScreenHandler>(x, y, 26, handler, { handler.result.getItem(0) }) {

    override fun onClick(button: Int) {
        c2s(Packets.CRAFTING_RESULT_SLOT_CLICK) {
            int(handler.containerId)
            int(button)
            bool(Screen.hasShiftDown())
        }
    }

}

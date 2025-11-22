package badasintended.slotlink.client.gui.widget

import net.minecraft.client.gui.components.AbstractWidget
import com.mojang.blaze3d.vertex.PoseStack

inline fun <T> AbstractWidget.bounds(action: (Int, Int, Int, Int) -> T): T {
    return action(x, y, width, height)
}

interface KeyGrabber {

    fun onKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean

}

interface CharGrabber {

    fun onChar(chr: Char, modifiers: Int): Boolean

}

interface TooltipRenderer {

    fun renderTooltip(matrices: PoseStack, mouseX: Int, mouseY: Int)

}
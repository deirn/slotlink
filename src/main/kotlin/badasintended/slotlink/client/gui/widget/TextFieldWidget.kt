package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.client
import badasintended.slotlink.util.focusedTicks
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.components.EditBox
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class TextFieldWidget(bgX: Int, bgY: Int, bgW: Int, bgH: Int, text: Component) :
    EditBox(client.font, bgX + 2, bgY + 2, bgW - 12, bgH - 3, text),
    CharGrabber,
    TooltipRenderer {

    var grab = false
        set(value) {
            field = value
            focusedTicks = 0
        }

    val tooltip = arrayListOf<Component>()

    init {
        setBordered(false)
        setTextColor(0xffffff)
    }

    override fun renderTooltip(matrices: PoseStack, mouseX: Int, mouseY: Int) {
        if (visible && !canConsumeInput()) client.screen?.renderComponentTooltip(matrices, tooltip, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isHovered) {
            grab = true
            if (isVisible && button == 1) setValue("")
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun canConsumeInput(): Boolean {
        return grab
    }

    override fun isFocused(): Boolean {
        return grab
    }

    override fun onChar(chr: Char, modifiers: Int): Boolean {
        return charTyped(chr, modifiers)
    }

}

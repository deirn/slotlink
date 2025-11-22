package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.GuiTextures
import badasintended.slotlink.client.util.bind
import badasintended.slotlink.client.util.client
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.AbstractWidget
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class ButtonWidget(x: Int, y: Int, w: Int, h: Int = w) : AbstractWidget(x, y, w, h, CommonComponents.EMPTY) {

    var texture = GuiTextures.FILTER
    var onPressed = { }
    var bgU = 0
    var bgV = 0
    var u = { 0 }
    var v = { 0 }
    var background = true
    var allowSpectator = false
    var tooltip: () -> Component? = { null }

    private var lastTooltip: Component? = null
    private var down = false
    private val padding = object {
        var l = 0
        var r = 0
        var t = 0
        var b = 0
    }

    fun padding(l: Int, t: Int = l, r: Int = l, b: Int = t) {
        padding.l = l
        padding.r = r
        padding.t = t
        padding.b = b
    }

    override fun renderWidget(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        val tooltip = this.tooltip()
        if (tooltip != lastTooltip) {
            setTooltip(Tooltip.create(tooltip))
            lastTooltip = tooltip
        }

        if (!visible) return
        texture.bind()

        if (background) {
            val u = if (isHovered) bgU + width else bgU
            blit(matrices, x, y, u, bgV, width, height)
        }

        padding.apply {
            blit(matrices, x + l, y + t, u(), v(), width - l - r, height - t - b)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!allowSpectator) {
            val player = client.player
            if (player != null && player.isSpectator) return false
        }

        if (isMouseOver(mouseX, mouseY)) {
            down = true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (down) onPressed()
        down = false
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput?) {}

}

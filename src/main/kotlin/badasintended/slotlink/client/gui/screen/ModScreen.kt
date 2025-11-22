package badasintended.slotlink.client.gui.screen

import badasintended.slotlink.client.gui.widget.CharGrabber
import badasintended.slotlink.client.gui.widget.KeyGrabber
import badasintended.slotlink.client.gui.widget.TooltipRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.components.AbstractWidget
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
abstract class ModScreen<H : AbstractContainerMenu>(h: H, inventory: Inventory, title: Component) :
    AbstractContainerScreen<H>(h, inventory, title) {

    abstract val baseTlKey: String

    private var clickedElement: AbstractWidget? = null
    var hoveredElement: AbstractWidget? = null

    fun tl(key: String, vararg args: Any) = Component.translatable("$baseTlKey.$key", *args)!!

    protected inline fun <T : AbstractWidget> add(t: T, func: T.() -> Unit = {}): T {
        return addRenderableWidget(t).apply(func)
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        if (menu.carried.isEmpty && hoveredSlot != null && hoveredSlot!!.hasItem()) {
            this.renderTooltip(matrices, hoveredSlot!!.item, mouseX, mouseY)
        } else {
            hoveredElement = getChildAt(mouseX.toDouble(), mouseY.toDouble()).orElse(null) as? AbstractWidget
            (hoveredElement as? TooltipRenderer)?.renderTooltip(matrices, mouseX, mouseY)
        }
    }

    override fun renderBg(matrices: PoseStack, delta: Float, mouseX: Int, mouseY: Int) {
        renderBackground(matrices)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)
        clickedElement = hoveredElement
        return result
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        isDragging = false
        val result = clickedElement?.mouseReleased(mouseX, mouseY, button) ?: false
        clickedElement = null
        return result || super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        focused?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return children().any { it is KeyGrabber && it.onKey(keyCode, scanCode, modifiers) }
            || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(char: Char, modifiers: Int): Boolean {
        return children().any { it is CharGrabber && it.onChar(char, modifiers) }
            || super.charTyped(char, modifiers)
    }

}

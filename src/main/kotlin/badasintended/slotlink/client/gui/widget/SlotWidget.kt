package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.client
import badasintended.slotlink.client.util.wrap
import badasintended.slotlink.compat.recipe.RecipeViewer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.world.item.TooltipFlag
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
abstract class SlotWidget<SH : AbstractContainerMenu>(
    x: Int, y: Int, s: Int,
    protected val handler: SH,
    private val stackGetter: () -> ItemStack
) : NoSoundWidget(x, y, s, s, CommonComponents.EMPTY),
    TooltipRenderer {

    val stack get() = stackGetter.invoke()

    private val stackX = x - 8 + width / 2
    private val stackY = y - 8 + height / 2

    abstract fun onClick(button: Int)

    protected open fun appendTooltip(tooltip: MutableList<Component>) {}

    protected open fun renderOverlay(matrices: PoseStack, stack: ItemStack) {
        client.apply {
            itemRenderer.renderGuiItemDecorations(matrices, font, stack, stackX, stackY)
        }
    }

    override fun renderTooltip(matrices: PoseStack, mouseX: Int, mouseY: Int) {
        matrices.wrap {
            matrices.translate(0.0, 0.0, +256.0)
            val x = stackX
            val y = stackY
            fill(matrices, x, y, x + 16, y + 16, -2130706433 /*0x80ffffff fuck*/)
            if (!stack.isEmpty && handler.carried.isEmpty && RecipeViewer.instance?.isDraggingStack != true)
                client.apply {
                    val tooltips = stack.getTooltipLines(
                        player,
                        TooltipFlag.Default(options.advancedItemTooltips, player?.isCreative ?: false)
                    )
                    appendTooltip(tooltips)
                    screen?.renderComponentTooltip(matrices, tooltips, mouseX, mouseY)
                }
        }
    }

    final override fun renderWidget(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) return
        client.itemRenderer.renderGuiItem(matrices, stack, stackX, stackY)
        renderOverlay(matrices, stack)
    }

    final override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val player = client.player ?: return false
        if (isHovered && visible && !player.isSpectator) {
            onClick(button)
            return true
        }
        return false
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput?) {}

}

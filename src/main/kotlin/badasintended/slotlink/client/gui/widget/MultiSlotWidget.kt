package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.client.util.client
import badasintended.slotlink.client.util.wrap
import badasintended.slotlink.init.Packets.MULTI_SLOT_ACTION
import badasintended.slotlink.screen.RequestScreenHandler
import badasintended.slotlink.util.enum
import badasintended.slotlink.util.int
import badasintended.slotlink.util.toFormattedString
import kotlin.math.ceil
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screens.Screen
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.ClickType.CLONE
import net.minecraft.world.inventory.ClickType.PICKUP
import net.minecraft.world.inventory.ClickType.QUICK_MOVE
import net.minecraft.world.inventory.ClickType.SWAP
import net.minecraft.world.inventory.ClickType.THROW
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting

/**
 * [MultiSlotWidget] is an impostor.
 */
@Environment(EnvType.CLIENT)
class MultiSlotWidget(
    handler: RequestScreenHandler,
    private val index: Int,
    x: Int, y: Int
) : SlotWidget<RequestScreenHandler>(x, y, 18, handler, { handler.itemViews[index].singleStack }),
    KeyGrabber {

    private val count get() = handler.itemViews[index].count

    override fun renderOverlay(matrices: PoseStack, stack: ItemStack) {
        client.apply {
            itemRenderer.renderGuiItemDecorations(matrices, font, stack, x + 1, y + 1, "")

            val factor = window.guiScale.toFloat()
            val scale = (1 / factor) * ceil(factor / 2)

            val countText = if (count <= 1) "" else count.toFormattedString()

            matrices.wrap {
                matrices.translate(0.0, 0.0, 250.0)
                matrices.scale(scale, scale, 1f)
                font.drawShadow(
                    matrices, countText, ((x + 17 - (font.width(countText) * scale)) / scale),
                    ((y + 17 - (font.lineHeight * scale)) / scale), 0xFFFFFF
                )
            }
        }
    }

    override fun appendTooltip(tooltip: MutableList<Component>) {
        (tooltip[0] as MutableComponent).append(Component.literal(" (${count})").withStyle(ChatFormatting.GOLD))
    }

    override fun onClick(button: Int) {
        c2s(MULTI_SLOT_ACTION) {
            int(handler.containerId)
            int(index)
            int(button)
            enum(if (button == 2) CLONE else if (Screen.hasShiftDown()) QUICK_MOVE else PICKUP)
        }
    }

    override fun onKey(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (isHovered) {
            if (client.options.keyDrop.matches(keyCode, scanCode)) {
                c2s(MULTI_SLOT_ACTION) {
                    int(handler.containerId)
                    int(index)
                    int(if (!Screen.hasControlDown()) 0 else 1)
                    enum(THROW)
                }
                return true
            }
            val hotbar = client.options.keyHotbarSlots.indexOfFirst { it.matches(keyCode, scanCode) }
            if (hotbar >= 0) {
                c2s(MULTI_SLOT_ACTION) {
                    int(handler.containerId)
                    int(index)
                    int(hotbar)
                    enum(SWAP)
                }
            }
        }
        return false
    }

}

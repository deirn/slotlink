package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.client.util.c2s
import badasintended.slotlink.client.util.client
import badasintended.slotlink.client.util.wrap
import badasintended.slotlink.compat.recipe.RecipeViewer
import badasintended.slotlink.init.Packets
import badasintended.slotlink.screen.FilterScreenHandler
import badasintended.slotlink.util.bool
import badasintended.slotlink.util.int
import badasintended.slotlink.util.stack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting

@Environment(EnvType.CLIENT)
class FilterSlotWidget(
    handler: FilterScreenHandler,
    private val index: Int,
    x: Int, y: Int
) : SlotWidget<FilterScreenHandler>(x, y, 18, handler, { handler.filter[index].first }) {

    private val nbt get() = handler.filter[index].second

    fun setStack(stack: ItemStack, nbt: Boolean = Screen.hasControlDown()) {
        handler.filterSlotClick(index, stack, nbt)
        c2s(Packets.FILTER_SLOT_CLICK) {
            int(handler.containerId)
            int(index)
            stack(stack)
            bool(nbt)
        }
    }

    override fun appendTooltip(tooltip: MutableList<Component>) {
        tooltip.add(Component.translatable("container.slotlink.filter.slot.nbt.${nbt}").withStyle(ChatFormatting.GRAY))
        tooltip.add(Component.translatable("container.slotlink.filter.slot.nbt.scroll").withStyle(ChatFormatting.GRAY))
    }

    override fun renderOverlay(matrices: PoseStack, stack: ItemStack) {
        super.renderOverlay(matrices, stack)

        client.apply {
            itemRenderer.renderGuiItemDecorations(matrices, font, stack, x + 1, y + 1, "")

            matrices.wrap {
                matrices.translate(0.0, 0.0, 250.0)
                fill(matrices, x + 1, y + 1, x + 17, y + 17, if (nbt) 0x70aa27ba else 0x408b8b8b)
                if (nbt) {
                    font.drawShadow(
                        matrices,
                        "+",
                        x + 17f - font.width("+"),
                        y + 17f - font.lineHeight,
                        0xaa27ba
                    )
                }
            }
        }
    }

    override fun renderTooltip(matrices: PoseStack, mouseX: Int, mouseY: Int) {
        super.renderTooltip(matrices, mouseX, mouseY)

        if (!handler.carried.isEmpty || RecipeViewer.instance?.isDraggingStack == true) matrices.wrap {
            matrices.translate(0.0, 0.0, +256.0)
            val tlKey = "container.slotlink.filter.slot.tip." +
                if (Screen.hasControlDown()) "pressed" else
                    if (Minecraft.ON_OSX) "cmd"
                    else "ctrl"
            client.screen?.renderTooltip(matrices, Component.translatable(tlKey), mouseX, mouseY)
        }
    }

    override fun onClick(button: Int) {
        setStack(handler.carried)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        val player = client.player ?: return false
        if (isHovered && visible && !player.isSpectator) {
            setStack(stack, !nbt)
            return true
        }
        return false
    }

}

@file:Environment(EnvType.CLIENT)

package badasintended.slotlink.client.util

import badasintended.slotlink.util.buf
import badasintended.slotlink.util.modId
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.Direction

fun Direction.texture(): ResourceLocation {
    return modId("textures/gui/side_${serializedName}.png")
}

val client: Minecraft
    get() = Minecraft.getInstance()

inline fun c2s(id: ResourceLocation, buf: FriendlyByteBuf.() -> Unit) {
    ClientPlayNetworking.send(id, buf().apply(buf))
}

object GuiTextures {

    val REQUEST = modId("textures/gui/request.png")
    val CRAFTING = modId("textures/gui/crafting.png")
    val FILTER = modId("textures/gui/filter.png")

}

fun ResourceLocation.bind() {
    RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
    RenderSystem.setShaderTexture(0, this)
}

inline fun PoseStack.wrap(action: () -> Unit) {
    pushPose()
    action()
    popPose()
}

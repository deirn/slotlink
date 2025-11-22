package badasintended.slotlink.client.gui.widget

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
abstract class NoSoundWidget(x: Int, y: Int, w: Int, h: Int, text: Component = CommonComponents.EMPTY) :
    AbstractWidget(x, y, w, h, text) {

    override fun playDownSound(soundManager: SoundManager) {}

    override fun updateWidgetNarration(builder: NarrationElementOutput?) {}

}
package badasintended.slotlink.compat.trinkets

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.player.Player

@Environment(EnvType.CLIENT)
object TrinketsAccess {

    var tryOpenRemote = { _: Player -> false }

}
package badasintended.slotlink.network

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

class Connection(
    pos: BlockPos,
    val type: NodeType<*>,
    val sides: HashSet<Direction> = hashSetOf()
) {

    val pos: BlockPos = pos.immutable()

    var sideBits: Int
        get() {
            var value = 0
            sides.forEach {
                value += 1 shl it.get3DDataValue()
            }
            return value
        }
        set(value) {
            sides.clear()
            for (i in 0 until 6) {
                if (((value shr i) and 1) != 0) {
                    sides.add(Direction.from3DDataValue(i))
                }
            }
        }

    override fun equals(other: Any?): Boolean {
        if (other !is Connection) return false
        if (other === this) return true

        return pos == other.pos && type == other.type
    }

    override fun hashCode(): Int {
        var result = pos.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

}

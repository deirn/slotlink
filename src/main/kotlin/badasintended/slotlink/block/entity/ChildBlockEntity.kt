package badasintended.slotlink.block.entity

import badasintended.slotlink.network.Connection
import badasintended.slotlink.network.Network
import badasintended.slotlink.network.Node
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.util.toArray
import badasintended.slotlink.util.toPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

abstract class ChildBlockEntity(
    blockEntityType: BlockEntityType<out BlockEntity>,
    nodeType: NodeType<*>,
    pos: BlockPos,
    state: BlockState
) : ModBlockEntity(blockEntityType, pos, state),
    Node {

    override val connection = Connection(pos, nodeType)

    private var lazyNetworkPos: BlockPos? = null
    private var lazyNetwork: Lazy<Network?>? = null
    private var _network: Network? = null
    override var network: Network?
        get() = _network ?: lazyNetwork?.value
        set(value) {
            _network = value
        }

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)

        network?.also {
            if (!it.deleted) nbt.putIntArray("network", it.masterPos.toArray())
        }
        nbt.putInt("sides", connection.sideBits)
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)

        lazyNetworkPos = if (nbt.contains("network")) nbt.getIntArray("network").toPos() else null
        connection.sideBits = nbt.getInt("sides")
    }

    override fun setLevel(world: Level?) {
        super.setLevel(world)
        lazyNetwork = lazy {
            lazyNetworkPos?.let { Network.get(world, it) }
        }
    }

    override fun setRemoved() {
        super.setRemoved()
        invalidate()
    }

}

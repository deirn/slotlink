package badasintended.slotlink.block.entity

import badasintended.slotlink.config.config
import badasintended.slotlink.init.BlockEntityTypes
import badasintended.slotlink.network.Connection
import badasintended.slotlink.network.Network
import badasintended.slotlink.network.Node
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.storage.NetworkStorage
import badasintended.slotlink.util.IntPair
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class MasterBlockEntity(pos: BlockPos, state: BlockState) :
    ModBlockEntity(BlockEntityTypes.MASTER, pos, state), Node {

    override val connection = Connection(pos, NodeType.MASTER)

    private val _network by lazy { Network.getOrCreate(level!!, pos) }
    override var network: Network?
        get() = _network
        set(_) {}

    var watchers = hashSetOf<Watcher>()

    private var tick = 0
    val forcedChunks = hashSetOf<IntPair>()

    fun getStorages(
        world: Level,
        flag: Int,
        request: Boolean = false
    ): NetworkStorage {
        val linkCables = _network
            .get(NodeType.LINK) { list -> list.sortedByDescending { it.priority } }
        val storages = linkCables
            .mapTo(ArrayList(linkCables.size)) { it.getStorage(world, Direction.UP, flag, this, request) }

        return NetworkStorage(storages)
    }

    fun unmarkForcedChunks() = level?.let { world ->
        if (!world.isClientSide && watchers.isEmpty()) {
            world as ServerLevel
            forcedChunks.forEach {
                world.setChunkForced(it.first, it.second, false)
            }
            forcedChunks.clear()
        }
    }

    fun markForcedChunks() = level?.let { world ->
        if (!world.isClientSide && watchers.isNotEmpty()) {
            world as ServerLevel
            forcedChunks.forEach {
                world.setChunkForced(it.first, it.second, true)
            }
        }
    }

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)
        nbt.putInt("sides", connection.sideBits)
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)
        connection.sideBits = nbt.getInt("sides")
    }

    override fun setRemoved() {
        super.setRemoved()
        invalidate()
        watchers.forEach { it.onMasterRemoved() }
    }

    object Ticker : BlockEntityTicker<MasterBlockEntity> {

        override fun tick(world: Level, pos: BlockPos, state: BlockState, masterBlockEntity: MasterBlockEntity) {
            if (!world.isClientSide) masterBlockEntity.apply {
                tick++
                if (tick == 10) {
                    if (config.pauseTransferWhenOnScreen && watchers.isNotEmpty()) return
                    val cables = _network.get(NodeType.IMPORT) { list ->
                        list.sortedByDescending { it.priority }
                    }
                    for (cable in cables) {
                        if (cable.transfer(world, this)) break
                    }
                } else if (tick == 20) {
                    tick = 0
                    if (config.pauseTransferWhenOnScreen && watchers.isNotEmpty()) return
                    val cables = _network.get(NodeType.EXPORT) { list ->
                        list.sortedByDescending { it.priority }
                    }
                    for (cable in cables) {
                        if (cable.transfer(world, this)) break
                    }
                }
            }
        }

    }

    interface Watcher {

        fun onMasterRemoved()

    }

}

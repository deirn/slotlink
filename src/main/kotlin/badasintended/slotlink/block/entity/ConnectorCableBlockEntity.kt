package badasintended.slotlink.block.entity

import badasintended.slotlink.block.ConnectorCableBlock
import badasintended.slotlink.network.Node
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.property.getNull
import badasintended.slotlink.storage.FilteredItemStorage
import badasintended.slotlink.util.int
import badasintended.slotlink.util.to
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor

@Suppress("UnstableApiUsage")
abstract class ConnectorCableBlockEntity(
    val block: ConnectorCableBlock,
    blockEntityType: BlockEntityType<out BlockEntity>,
    nodeType: NodeType<*>,
    pos: BlockPos,
    state: BlockState
) : FilteredBlockEntity(blockEntityType, nodeType, pos, state) {

    private var apiCache: BlockApiCache<Storage<ItemVariant>, Direction?>? = null

    private var linkedSide = state.getNull(ConnectorCableBlock.CONNECTED)
        set(value) {
            apiCache = null
            field = value
        }

    private var linkedPos: BlockPos? = linkedSide?.let { pos.relative(it) }
        set(value) {
            apiCache = null
            field = value
        }

    var priority = 0
        set(value) {
            invalidate()
            field = value
        }

    fun getStorage(
        world: LevelAccessor,
        side: Direction,
        flag: Int,
        master: MasterBlockEntity? = null,
        request: Boolean = false
    ): FilteredItemStorage {
        if (linkedPos == null) return FilteredItemStorage.EMPTY
        if (world !is ServerLevel) return FilteredItemStorage.EMPTY

        if (master != null && request) {
            val chunkPos = ChunkPos(worldPosition)
            if (!world.forcedChunks.contains(chunkPos.toLong())) {
                master.forcedChunks.add(chunkPos.x to chunkPos.z)
            }
        }

        val linkedState = world.getBlockState(linkedPos)

        if (!block.isIgnored(linkedState)) {
            if (apiCache == null) apiCache = BlockApiCache.create(ItemStorage.SIDED, world, linkedPos)
            val blockEntity = apiCache!!.blockEntity
            if (blockEntity is Node && blockEntity.network == this.network) return FilteredItemStorage.EMPTY
            val storage = apiCache!!.find(side) ?: return FilteredItemStorage.EMPTY
            return FilteredItemStorage(filter, blacklist, flag, storage, priority, linkedPos!!)
        } else {
            return FilteredItemStorage.EMPTY
        }
    }

    override fun connect(adjacentNode: Node?): Boolean {
        return if (adjacentNode is InterfaceBlockEntity) false else super.connect(adjacentNode)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun setBlockState(state: BlockState) {
        super.setBlockState(state)
        linkedSide = state.getNull(ConnectorCableBlock.CONNECTED)
        linkedPos = linkedSide?.let { worldPosition.relative(it) }
    }

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)

        nbt.putInt("priority", priority)
        linkedSide?.let { nbt.putInt("link", it.get3DDataValue()) }
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)

        linkedSide = if (nbt.contains("link")) Direction.from3DDataValue(nbt.getInt("link")) else null
        linkedPos = linkedSide?.let { worldPosition.relative(it) }
        priority = nbt.getInt("priority")
    }

    override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
        super.writeScreenOpeningData(player, buf)
        buf.apply {
            int(priority)
        }
    }

}

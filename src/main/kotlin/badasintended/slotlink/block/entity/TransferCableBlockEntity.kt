package badasintended.slotlink.block.entity

import badasintended.slotlink.block.ConnectorCableBlock
import badasintended.slotlink.block.entity.TransferCableBlockEntity.Mode.NEGATIVE
import badasintended.slotlink.block.entity.TransferCableBlockEntity.Mode.OFF
import badasintended.slotlink.block.entity.TransferCableBlockEntity.Mode.ON
import badasintended.slotlink.block.entity.TransferCableBlockEntity.Mode.POSITIVE
import badasintended.slotlink.network.NodeType
import badasintended.slotlink.screen.TransferCableScreenHandler
import badasintended.slotlink.util.int
import badasintended.slotlink.util.isEmpty
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.server.level.ServerPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

@Suppress("UnstableApiUsage")
abstract class TransferCableBlockEntity(
    block: ConnectorCableBlock,
    blockEntityType: BlockEntityType<out BlockEntity>,
    nodeType: NodeType<*>,
    pos: BlockPos,
    state: BlockState
) : ConnectorCableBlockEntity(block, blockEntityType, nodeType, pos, state) {

    var mode = OFF
    abstract var side: Direction

    protected abstract fun getSource(world: Level, master: MasterBlockEntity): Storage<ItemVariant>
    protected abstract fun getTarget(world: Level, master: MasterBlockEntity): Storage<ItemVariant>

    fun transfer(world: Level, master: MasterBlockEntity): Boolean {
        when (mode) {
            OFF -> return false
            ON -> Unit
            POSITIVE -> if (world.getBestNeighborSignal(worldPosition) <= 0) return false
            NEGATIVE -> if (world.getBestNeighborSignal(worldPosition) > 0) return false
        }

        val source = getSource(world, master)
        if (!source.supportsExtraction()) return false

        val target = getTarget(world, master)
        if (!target.supportsInsertion()) return false

        Transaction.openOuter().use { transaction ->
            for (view in source) {
                if (view.isEmpty) continue
                val variant = view.resource
                val available = transaction.openNested().use { simulation ->
                    view.extract(variant, variant.item.maxStackSize.toLong(), simulation)
                }
                val inserted = target.insert(variant, available, transaction)
                if (inserted > 0) {
                    view.extract(variant, inserted, transaction)
                    transaction.commit()
                    return true
                }
            }
        }

        return false
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)

        side = Direction.from3DDataValue(nbt.getInt("side"))
        mode = Mode.of(nbt.getInt("mode"))
    }

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)

        nbt.putInt("side", side.get3DDataValue())
        nbt.putInt("mode", mode.ordinal)
    }

    override fun createMenu(syncId: Int, inv: Inventory, player: Player) = TransferCableScreenHandler(
        syncId, inv, blacklist, filter, priority, side, mode, ContainerLevelAccess.create(level, worldPosition)
    )

    override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
        super.writeScreenOpeningData(player, buf)
        buf.apply {
            int(side.get3DDataValue())
            int(mode.ordinal)
        }
    }

    enum class Mode(
        private val id: String
    ) {

        ON("on"),
        POSITIVE("positive"),
        NEGATIVE("negative"),
        OFF("off");

        companion object {

            val values = values()
            fun of(i: Int) = values[i.coerceIn(0, 3)]

        }

        fun next(): Mode {
            return values[(ordinal + 1) % values.size]
        }

        override fun toString() = id

    }

}

package badasintended.slotlink.network

import kotlin.reflect.safeCast
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class Network internal constructor(
    val state: NetworkState?,
    val world: Level,
    val masterPos: BlockPos
) {

    companion object {

        fun get(world: Level?, pos: BlockPos): Network? {
            if (world !is ServerLevel) return null
            return NetworkState[world][pos]
        }

        fun getOrCreate(world: Level, pos: BlockPos): Network {
            if (world !is ServerLevel) return Network(null, world, pos)

            val state = NetworkState[world]
            return state.getOrPut(pos) {
                Network(state, world, pos)
            }
        }

    }

    val master get() = get(NodeType.MASTER).firstOrNull()

    private var _deleted = false
    val deleted get() = _deleted

    val map = hashMapOf<BlockPos, NodeType<*>>()
    val cache = hashMapOf<NodeType<*>, List<Node>>()


    init {
        map[masterPos] = NodeType.MASTER
    }

    fun add(node: Node) {
        if (world.isClientSide) return
        map[node.connection.pos] = node.connection.type
        markDirty()
        invalidate(node.connection.type)
    }

    fun remove(node: Node) {
        if (world.isClientSide) return
        map.remove(node.connection.pos)
        markDirty()
        invalidate(node.connection.type)
    }

    fun invalidate(type: NodeType<*>) {
        if (world.isClientSide) return
        cache.remove(type)
    }

    fun delete() {
        if (world.isClientSide) return
        _deleted = true
        map.clear()
        cache.clear()
        state?.remove(masterPos)
        markDirty()
    }

    private fun markDirty() {
        if (world.isClientSide) return
        state?.isDirty = true
    }

    fun validate() {
        if (world.isClientSide) return

        val unvisited = HashSet(map.keys)
        fun visit(pos: BlockPos, adjacentNode: Node?) {
            if (!unvisited.remove(pos)) {
                if (map.contains(pos) || adjacentNode == null) return
                val newConnection = world.getBlockEntity(pos) as? Node ?: return
                if (!newConnection.connect(adjacentNode)) return
            }

            get(pos) { node ->
                node.connection.sides.forEach { side ->
                    visit(pos.relative(side), node)
                }
            }
        }

        visit(masterPos, null)

        unvisited.forEach { pos ->
            get(pos) {
                remove(it)
                it.network = null
            }
        }
    }

    inline fun get(pos: BlockPos, consumer: (Node) -> Unit) {
        if (world.isClientSide || !map.containsKey(pos)) return
        val be = world.getBlockEntity(pos)
        (be as? Node)?.apply {
            consumer(this)
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <T : Node> get(
        type: NodeType<T>,
        transformer: (List<T>) -> List<T> = { it }
    ): List<T> {
        if (world.isClientSide) return emptyList()
        return cache.getOrPut(type) {
            transformer(map
                .filterValues { it == type }
                .mapNotNull { type.clazz.safeCast(world.getBlockEntity(it.key)) })
        } as List<T>
    }

}

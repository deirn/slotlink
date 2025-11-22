package badasintended.slotlink.network

import badasintended.slotlink.util.toArray
import badasintended.slotlink.util.toPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.saveddata.SavedData

class NetworkState : SavedData() {

    companion object {

        operator fun get(world: ServerLevel): NetworkState {
            world as NetworkStateHolder
            return world.networkState
        }

        @JvmStatic
        fun create(world: ServerLevel, nbt: CompoundTag): NetworkState {
            return NetworkState().apply {
                if (nbt.contains("networks")) {
                    val networks = nbt.getList("networks", Tag.TAG_COMPOUND.toInt())
                    networks.forEach { obj ->
                        obj as CompoundTag
                        val masterPos = obj.getIntArray("master").toPos()
                        map[masterPos] = Network(this, world, masterPos).also { network ->
                            val posses = obj.getList("pos", Tag.TAG_INT_ARRAY.toInt())
                            posses.forEach { pos ->
                                pos as IntArrayTag
                                val arr = pos.asIntArray
                                network.map[arr.toPos()] = NodeType[arr[3]]
                            }
                        }
                    }
                }
            }
        }

    }

    val map = hashMapOf<BlockPos, Network>()

    operator fun get(pos: BlockPos) = map[pos]
    inline fun getOrPut(pos: BlockPos, default: () -> Network) = map.getOrPut(pos, default)
    fun remove(pos: BlockPos) = map.remove(pos)

    override fun save(nbt: CompoundTag): CompoundTag {
        if (map.isNotEmpty()) {
            val list = ListTag()
            map.forEach { (masterPos, network) ->
                if (!network.deleted && network.map.isNotEmpty()) {
                    val obj = CompoundTag()
                    obj.putIntArray("master", masterPos.toArray())
                    val posses = ListTag()
                    network.map.forEach { (pos, type) ->
                        if (type.save) {
                            posses.add(IntArrayTag(intArrayOf(pos.x, pos.y, pos.z, type.index)))
                        }
                    }
                    obj.put("pos", posses)
                    list.add(obj)
                }
            }
            nbt.put("networks", list)
        }
        return nbt
    }

}
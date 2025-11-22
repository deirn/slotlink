package badasintended.slotlink.util

import badasintended.slotlink.Slotlink
import io.netty.buffer.Unpooled
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import org.slf4j.LoggerFactory

typealias BlockEntityBuilder = (BlockPos, BlockState) -> BlockEntity

fun BlockPos.toArray(): IntArray {
    return intArrayOf(x, y, z)
}

fun IntArray.toPos(): BlockPos {
    return BlockPos(get(0), get(1), get(2))
}

fun Player.actionBar(key: String, vararg args: Any) {
    displayClientMessage(Component.translatable(key, *args), true)
}

fun buf(): FriendlyByteBuf {
    return FriendlyByteBuf(Unpooled.buffer())
}

/**
 * Generates [VoxelShape] based on the position that shows on [Blockbench](https://blockbench.net).
 * No thinking required!
 */
fun bbCuboid(xPos: Int, yPos: Int, zPos: Int, xSize: Int, ySize: Int, zSize: Int): VoxelShape {
    val xMin = xPos / 16.0
    val yMin = yPos / 16.0
    val zMin = zPos / 16.0
    val xMax = (xPos + xSize) / 16.0
    val yMax = (yPos + ySize) / 16.0
    val zMax = (zPos + zSize) / 16.0
    return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax)
}

fun Direction.next(): Direction {
    return Direction.from3DDataValue(get3DDataValue() + 1)
}

fun FriendlyByteBuf.writeFilter(filter: List<ObjBoolPair<ItemStack>>) {
    filter.forEach {
        writeItem(it.first)
        writeBoolean(it.second)
    }
}

fun FriendlyByteBuf.readFilter(size: Int = 9): MutableList<ObjBoolPair<ItemStack>> {
    val list = arrayListOf<ObjBoolPair<ItemStack>>()
    for (i in 0 until size) {
        list.add(readItem() to readBoolean())
    }
    return list
}

fun modId(path: String) = ResourceLocation(Slotlink.ID, path)

@Suppress("unused")
val log = LoggerFactory.getLogger(Slotlink.ID)!!

inline fun s2c(player: Player, id: ResourceLocation, buf: FriendlyByteBuf.() -> Unit) {
    player as ServerPlayer
    ServerPlayNetworking.send(player, id, buf().apply(buf))
}

fun s2c(player: Player, packet: Packet<*>) {
    player as ServerPlayer
    ServerPlayNetworking.getSender(player).sendPacket(packet)
}

val ignoredTag: TagKey<Block> = TagKey.create(Registries.BLOCK, modId("ignored"))

fun ItemStack.isItemAndTagEqual(other: ItemStack): Boolean {
    return ItemStack.isSame(this, other) && ItemStack.tagMatches(this, other)
}

fun ItemStack.merge(from: ItemStack): Pair<ItemStack, ItemStack> {
    val f = from.copy()
    val t = this.copy()

    if (isEmpty) return f to ItemStack.EMPTY
    if (!isItemAndTagEqual(f) || count >= maxStackSize || f.isEmpty) return t to f

    val max = (maxStackSize - count).coerceAtLeast(0)
    val added = min(max, f.count)

    t.grow(added)
    f.shrink(added)

    return t to f
}

fun Pair<ItemStack, ItemStack>.allEmpty() = first.isEmpty && second.isEmpty

var ObjIntPair<Container>.stack: ItemStack
    get() = first.getItem(second)
    set(value) = first.setItem(second, value)


fun Int.toFormattedString(): String = when {
    this < 1000 -> "$this"
    else -> {
        val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()
        String.format("%.1f%c", this / 1000.0.pow(exp.toDouble()), "KMGTPE"[exp - 1])
    }
}

inline fun modLoaded(modid: String, action: () -> Unit) {
    if (FabricLoader.getInstance().isModLoaded(modid)) action()
}

@file:Suppress("HasPlatformType", "NOTHING_TO_INLINE")

package badasintended.slotlink.util

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.FriendlyByteBuf as B

inline fun B.bool(boolean: Boolean) = writeBoolean(boolean)
inline val B.bool get() = readBoolean()

inline fun B.int(int: Int) = writeVarInt(int)
inline val B.int get() = readVarInt()

inline fun B.string(string: String) = writeUtf(string)
inline val B.string get() = readUtf(32767)

inline fun B.stack(stack: ItemStack) = writeItem(stack)
inline val B.stack get() = readItem()

inline fun B.item(item: Item) = writeVarInt(BuiltInRegistries.ITEM.getId(item))
inline val B.item get() = BuiltInRegistries.ITEM.byId(readVarInt())

inline fun B.nbt(nbt: CompoundTag?) = writeNbt(nbt)
inline val B.nbt get() = readNbt()

inline fun B.id(id: ResourceLocation) = writeResourceLocation(id)
inline val B.id get() = readResourceLocation()

inline fun B.enum(enum: Enum<*>) = writeVarInt(enum.ordinal)
inline fun <reified T : Enum<T>> B.enum() = enumValues<T>()[int]

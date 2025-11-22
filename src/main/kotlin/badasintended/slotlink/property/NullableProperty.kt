package badasintended.slotlink.property

import badasintended.slotlink.property.NullableProperty.Value
import java.util.*
import net.minecraft.world.level.block.state.StateHolder
import net.minecraft.world.level.block.state.properties.Property

fun <O, S, T : Comparable<T>> StateHolder<O, S>.with(property: NullableProperty<T>, actualValue: T?): S {
    val value = if (actualValue == null) property.nullValue else property.map[actualValue]!!
    return setValue(property, value)
}

fun <O, S, T : Comparable<T>> StateHolder<O, S>.getNull(property: NullableProperty<T>): T? {
    return getValue(property).value
}

@Suppress("UNCHECKED_CAST")
class NullableProperty<T : Comparable<T>>(
    private val property: Property<T>
) : Property<Value<T>>(property.name, Value::class.java as Class<Value<T>>) {

    class Value<T : Comparable<T>> internal constructor(
        val value: T?
    ) : Comparable<Value<T>> {

        override fun compareTo(other: Value<T>): Int {
            if (other === this || (value == null && other.value == null)) return 0

            if (value == null) return -1
            if (other.value == null) return 1

            return value.compareTo(other.value)
        }

        override fun toString() = value?.toString() ?: "null"

    }

    private val values = linkedSetOf<Value<T>>()

    internal val map = hashMapOf<T, Value<T>>()
    internal val nullValue = Value<T>(null)

    init {
        values.add(nullValue)
        property.possibleValues.forEach {
            val value = Value(it)
            values.add(value)
            map[it] = value
        }
    }

    override fun getPossibleValues(): MutableCollection<Value<T>> = values

    override fun getName(value: Value<T>) = value.value?.toString() ?: "null"

    override fun getValue(name: String): Optional<Value<T>> {
        return if (name == "null") Optional.of(nullValue) else property.getValue(name).map(map::get)
    }

}
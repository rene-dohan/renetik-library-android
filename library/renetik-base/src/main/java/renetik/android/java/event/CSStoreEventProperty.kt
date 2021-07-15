package renetik.android.java.event

import renetik.android.framework.CSValueStoreInterface
import renetik.android.framework.getValue
import renetik.android.java.event.CSEventPropertyFunctions.property

class CSListStoreEventProperty<T>(
    store: CSValueStoreInterface, val key: String, val values: List<T>,
    val default: T, onApply: ((value: T) -> Unit)? = null)
    : CSEventProperty<T>(store.getValue(key, values, default), onApply) {

    init {
        onChange { store.save(key, it.hashCode()) }
    }

    var store: CSValueStoreInterface = store
        set(value) {
            field = value
            value(store.getValue(key, values, default), true)
        }
}

val <T : Enum<*>> CSListStoreEventProperty<T>.isLast get() = values.lastIndex == value.ordinal
fun <T : Enum<*>> CSListStoreEventProperty<T>.next(): T = values[value.ordinal + 1]
fun <T : Enum<*>> CSListStoreEventProperty<T>.previous(): T = values[value.ordinal - 1]

fun CSValueStoreInterface.property(
    key: String, default: Boolean, onApply: ((value: Boolean) -> Unit)? = null
) = property(getBoolean(key, default), onApply)
    .apply { onChange { save(key, it) } }

fun <T> CSValueStoreInterface.property(
    key: String, values: List<T>, default: T, onApply: ((value: T) -> Unit)? = null
) = CSListStoreEventProperty(this, key, values, default, onApply)

fun <T> CSValueStoreInterface.property(
    key: String, values: Array<T>, default: T, onApply: ((value: T) -> Unit)? = null
) = property(key, values.asList(), default, onApply)

fun CSValueStoreInterface.property(
    key: String, default: Int, onApply: ((value: Int) -> Unit)? = null
) = property(getInt(key, default), onApply)
    .apply { onChange { save(key, it) } }

fun CSValueStoreInterface.property(
    key: String, default: Float,
    onApply: ((value: Float) -> Unit)? = null
) = property(getFloat(key, default), onApply)
    .apply { onChange { save(key, it) } }

fun CSValueStoreInterface.property(
    key: String, default: Float?,
    onApply: ((value: Float?) -> Unit)? = null
) = property(getFloat(key, default), onApply)
    .apply { onChange { save(key, it) } }

fun CSValueStoreInterface.property(
    key: String, default: String, onChange: ((value: String) -> Unit)? = null
) = property(getString(key, default), onChange)
    .apply { onChange { save(key, it) } }
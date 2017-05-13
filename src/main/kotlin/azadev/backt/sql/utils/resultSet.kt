package azadev.backt.sql.utils

import java.sql.ResultSet
import java.util.*


inline fun <T> ResultSet.single(creator: (ResultSet)->T): T? {
	if (next())
		return creator(this)
	return null
}

inline fun <T> ResultSet.toList(size: Int = 100, creator: (ResultSet)->T): List<T> {
	val list = ArrayList<T>(size)
	while (next())
		list.add(creator(this))
	return list
}
inline fun <T> ResultSet.toList(creator: (ResultSet)->T) = toList(2, creator)

inline fun <K, V> ResultSet.toMap(size: Int = 100, creator: (ResultSet)->Pair<K, V>): Map<K, V> {
	val map = HashMap<K, V>(size)
	while (next())
		creator(this).run { map.put(first, second) }
	return map
}
inline fun <K, V> ResultSet.toMap(creator: (ResultSet)->Pair<K, V>) = toMap(2, creator)

val ResultSet?.count: Int
	get() = if (this != null && next()) getInt(1) else 0
val ResultSet?.countByte: Byte
	get() = if (this != null && next()) getByte(1) else 0
val ResultSet?.countLong: Long
	get() = if (this != null && next()) getLong(1) else 0L
val ResultSet?.countFloat: Float
	get() = if (this != null && next()) getFloat(1) else 0f
val ResultSet?.countDouble: Double
	get() = if (this != null && next()) getDouble(1) else .0

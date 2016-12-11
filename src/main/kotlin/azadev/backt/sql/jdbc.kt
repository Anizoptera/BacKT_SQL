package azadev.backt.sql

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


/**
 * Joins array of column names to string with placeholders:
 * "col1", "col2", "col3" >> "col1=?,col2=?,col3=?"
 */
val Array<out String>.asParameterized: String
	get() = joinToString("=?,") + "=?"

/**
 * Creates a string that can be passed into a VALUE-clause:
 * `... VALUE (value1, value2[0]), (value1, value2[1]), (value1, value2[2])`
 *
 * Examples:
 * makeValueSet()                                   ---> ()
 * makeValueSet(2, arrayOf(1,2,3)))                 ---> (2,1),(2,2),(2,3)
 * makeValueSet(listOf(1,4,6), 2, arrayOf(3,5,7)))  ---> (1,2,3),(4,2,5),(6,2,7)
 */
fun makeValueSet(vararg values: Any): String {
	val sb = StringBuilder()
	val argsLastIdx = values.lastIndex
	var i = -1
	var maxIdx = 0

	while (++i <= maxIdx) {
		sb.append('(')
		for (j in 0..argsLastIdx) {
			var value = values[j]

			if (value is Array<*>)
				value = value.asList()

			if (value is List<*>) {
				if (value.size > maxIdx)
					maxIdx = value.lastIndex

				sb.append(value[i])
			}
			else sb.append(value)

			if (j < argsLastIdx)
				sb.append(',')
		}
		sb.append(')')

		if (i < maxIdx)
			sb.append(',')
	}

	return sb.toString()
}

@file:Suppress("unused")

package azadev.backt.sql

import java.sql.ResultSet
import java.util.*


class QueryBuilder
{
	val sb = StringBuilder(15) // SELECT * FROM t

	var params: ArrayList<Any?>? = null
	val paramArray: Array<Any?> get() = params?.toArray() ?: emptyArray()

	var hasWhere = false
	var hasOrder = false


	fun executeQuery(db: Database): ResultSet {
		if (params == null)
			return db.executeQuery(toString())
		return db.executeQuery(toString(), *paramArray)
	}

	fun executeUpdate(db: Database): Int {
		if (params == null)
			return db.executeUpdate(toString())
		return db.executeUpdate(toString(), *paramArray)
	}

	fun executeUpdateWithAutoKeys(db: Database): ResultSet? {
		if (params == null)
			return db.executeUpdateWithAutoKeys(toString())
		return db.executeUpdateWithAutoKeys(toString(), *paramArray)
	}


	override fun toString() = sb.toString()


	fun p(param: Any?): QueryBuilder {
		params = (params ?: ArrayList<Any?>(1)).apply { add(param) }
		return this
	}


	fun selectAll(): QueryBuilder {
		sb.append("SELECT *")
		return this
	}
	fun select(col: String): QueryBuilder {
		sb.append("SELECT ").appendQuoted(col)
		return this
	}
	fun select(vararg cols: String): QueryBuilder {
		sb.append("SELECT ")
		cols.forEachIndexed { i,col ->
			if (i > 0) sb.append(',')
			sb.appendQuoted(col)
		}
		return this
	}


	fun from(table: String): QueryBuilder {
		sb.append(" FROM ").appendQuoted(table)
		return this
	}


	fun where(col: String, value: Any?) = where0(col, '=', value)
	fun wherep(col: String, param: Any?) = where0(col, '=', '?').p(param)
	fun whereNull(col: String) = where0(col, " IS ", "NULL")

	fun whereGt(col: String, value: Any?) = where0(col, '>', value)
	fun wherepGt(col: String, param: Any?) = where0(col, '>', '?').p(param)

	fun whereLt(col: String, value: Any?) = where0(col, '<', value)
	fun wherepLt(col: String, param: Any?) = where0(col, '<', '?').p(param)

	fun whereNot(col: String, value: Any?) = where0(col, "<>", value)
	fun wherepNot(col: String, param: Any?) = where0(col, "<>", '?').p(param)
	fun whereNotNull(col: String) = where0(col, " IS NOT ", "NULL")

	fun whereIn(col: String, values: Any): QueryBuilder {
		where0(col, false, " IN (")
		smartAppendJoined(values)
		sb.append(')')
		return this
	}
	fun wherepIn(col: String, param: Any?): QueryBuilder {
		whereIn(col, '?').p(param)
		return this
	}

	private fun where0(col: String, eq: Any, value: Any?): QueryBuilder {
		if (!hasWhere) {
			sb.append(" WHERE ")
			hasWhere = true
		}
		else sb.append(" AND ")

		sb.appendQuoted(col)

		when (eq) {
			is Char -> sb.append(eq)
			is String -> sb.append(eq)
		}

		smartAppend(value)

		return this
	}


	fun orderBy(col: String, desc: Boolean = false): QueryBuilder {
		if (!hasOrder) {
			sb.append(" ORDER BY ")
			hasOrder = true
		}
		else sb.append(',')

		sb.appendQuoted(col)

		if (desc)
			sb.append(" DESC")

		return this
	}


	fun limit(num: Int): QueryBuilder {
		sb.append(" LIMIT ").append(num)
		return this
	}


	private fun StringBuilder.appendQuoted(col: String)
			= append('`').append(col).append('`')

	private fun smartAppend(value: Any?) {
		// Common types (to avoid "toString" insude StringBuilder)
		when (value) {
			is Boolean -> sb.append(if (value) 1 else 0)

			is Long -> sb.append(value)
			is Int -> sb.append(value)
			is Short -> sb.append(value)
			is Byte -> sb.append(value)
			is Double -> sb.append(value)
			is Float -> sb.append(value)

			is String -> sb.append(value) // Avoid adding String directly. Use placeholder '?'
			is Char -> sb.append(value)

			else -> sb.append(value)
		}
	}

	private fun smartAppendJoined(values: Any) {
		var i = -1
		when (values) {
			is Array<*> -> values.forEach {
				if (++i > 0) sb.append(',')
				smartAppend(it)
			}
			is Iterable<*> -> values.forEach {
				if (++i > 0) sb.append(',')
				smartAppend(it)
			}
			else -> smartAppend(values)
		}
	}
}

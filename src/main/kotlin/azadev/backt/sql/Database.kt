package azadev.backt.sql

import java.sql.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * We use the approach to set params like "Exposed" does:
 * https://github.com/JetBrains/Exposed/blob/f612f142140427ba2aca256c6a4415edac78a39f/src/main/kotlin/org/jetbrains/exposed/sql/ColumnType.kt#L52
 *
 * Another approach to set params:
 * https://github.com/seratch/kotliquery/blob/master/src/main/kotlin/kotliquery/Session.kt#L33
 *
 * for (i in params.indices) {
 * 	val p = params.get(i)
 * 	when (p) {
 * 		is String -> stmt.setString(i, p)
 * 		is Byte -> stmt.setByte(i, p)
 * 		is Boolean -> stmt.setBoolean(i, p)
 * 		is Int -> stmt.setInt(i, p)
 * 		is Long -> stmt.setLong(i, p)
 * 		is Short -> stmt.setShort(i, p)
 * 		is Double -> stmt.setDouble(i, p)
 * 		is Float -> stmt.setFloat(i, p)
 * 		is BigDecimal -> stmt.setBigDecimal(i, p)
 *
 * 		is ByteArray -> stmt.setBytes(i, p)
 * 		is java.sql.Array -> stmt.setArray(i, p)
 * 		is InputStream -> stmt.setBinaryStream(i, p)
 * 		is URL -> stmt.setURL(i, p)
 *
 * 		is ZonedDateTime -> stmt.setTimestamp(i, Timestamp(Date.from(p.toInstant()).time))
 * 		is OffsetDateTime -> stmt.setTimestamp(i, Timestamp(Date.from(p.toInstant()).time))
 * 		is Instant -> stmt.setTimestamp(i, Timestamp(Date.from(p).time))
 * 		//is LocalDateTime -> stmt.setTimestamp(i, Timestamp(org.joda.time.LocalDateTime.parse(p.toString()).toDate().time))
 * 		//is LocalDate -> stmt.setDate(i, java.sql.Date(org.joda.time.LocalDate.parse(p.toString()).toDate().time))
 * 		//is LocalTime -> stmt.setTime(i, java.sql.Time(org.joda.time.LocalTime.parse(p.toString()).toDateTimeToday().millis))
 * 		//is org.joda.time.DateTime -> stmt.setTimestamp(i, Timestamp(p.toDate().time))
 * 		//is org.joda.time.LocalDateTime -> stmt.setTimestamp(i, Timestamp(p.toDate().time))
 * 		//is org.joda.time.LocalDate -> stmt.setDate(i, java.sql.Date(p.toDate().time))
 * 		//is org.joda.time.LocalTime -> stmt.setTime(i, java.sql.Time(p.toDateTimeToday().millis))
 * 		is java.util.Date -> stmt.setTimestamp(i, Timestamp(p.time))
 * 		is java.sql.Timestamp -> stmt.setTimestamp(i, p)
 * 		is java.sql.Time -> stmt.setTime(i, p)
 * 		is java.sql.Date -> stmt.setTimestamp(i, Timestamp(p.time))
 * 		is java.sql.SQLXML -> stmt.setSQLXML(i, p)
 *
 * 		else -> stmt.setObject(i, p)
 * 	}
 * }
 */

class Database
{
	lateinit var connection: Connection

	val statements = ConcurrentLinkedQueue<Statement>()


	fun connect(url: String, user: String? = null, password: String? = null) {
		val props = Properties()
		if (user != null) props.put("user", user)
		if (password != null) props.put("password", password)
		connection = DriverManager.getConnection(url, props)
	}

	fun close() {
		closeStatements()
		connection.close()
	}

	/**
	 * Closes all the open statements and clears the statement list.
	 *
	 * Note that this method is NOT thread safe. Don't call this method
	 * and any 'execute*' methods simultaneously from different threads.
	 * This may lead to leak of unclosed statements.
	 */
	fun closeStatements() {
		for (s in statements)
			s.close()
		statements.clear()
	}


	fun executeQuery(sql: String, vararg params: Any?): ResultSet {
		val stmt = connection.prepareStatement(sql)

		for (i in params.indices)
			stmt.setObject(i+1, params[i])

		statements.add(stmt)
		return stmt.executeQuery()
	}

	fun executeUpdate(sql: String, vararg params: Any?): Int {
		val stmt = connection.prepareStatement(sql)

		for (i in params.indices)
			stmt.setObject(i+1, params[i])

		statements.add(stmt)
		return stmt.executeUpdate()
	}

	fun executeUpdateWithAutoKeys(sql: String, vararg params: Any?): ResultSet? {
		val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

		for (i in params.indices)
			stmt.setObject(i+1, params[i])

		if (stmt.executeUpdate() <= 0)
			return null

		statements.add(stmt)
		return stmt.generatedKeys
	}


	fun disableAutoCommit() {
		connection.autoCommit = false
	}

	fun enableAutoCommit() {
		connection.autoCommit = true
	}

	fun commit(_enableAutoCommit: Boolean = true) {
		if (!connection.autoCommit)
			connection.commit()

		if (_enableAutoCommit)
			enableAutoCommit()
	}

	fun rollback(_enableAutoCommit: Boolean = true) {
		if (!connection.autoCommit)
			connection.rollback()

		if (_enableAutoCommit)
			enableAutoCommit()
	}


	companion object
	{
		fun loadDriver(driver: String) {
			// com.mysql.jdbc.Driver
			// org.postgresql.Driver
			Class.forName(driver)
		}

		fun createConnection(url: String, user: String? = null, password: String? = null): Database {
			val db = Database()
			db.connect(url, user, password)
			return db
		}
	}
}

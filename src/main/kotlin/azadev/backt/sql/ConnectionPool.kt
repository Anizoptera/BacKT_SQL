package azadev.backt.sql

import azadev.logging.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger


class ConnectionPool(
		val maxSize: Int,
		val connectionCreator: ()->Database
) {
	private var availableQueue = ConcurrentLinkedQueue<Database>()
	private var obtainedQueue = ConcurrentLinkedQueue<Database>()

	private val createdCount = AtomicInteger(0)
	private val obtainingCount = AtomicInteger(0)

	@Volatile
	private var isResetting = false


	fun obtain(): Database {
		logVerbose("Obtaining a connection")

		/*
		 We cannot just check the "isResetting" flag and, if it's "false", increment the counter,
		 because between these 2 events the "reset()" method might start to work in another thread,
		 and quickly perform all its initial checks (yes, it should be much faster thread, but still).
		 */
		obtainingCount.incrementAndGet()
		if (isResetting) {
			obtainingCount.decrementAndGet()
			while (isResetting) {}
			obtainingCount.incrementAndGet()
		}

		var db = availableQueue.poll()
		if (db == null) {
			val c = createdCount.get()
			if (c < maxSize) {
				logVerbose("Creating a new connection")

				if (!createdCount.compareAndSet(c, c+1)) {
					logVerbose("Created counter changed. Re-obtaining ...")
					return obtain()
				}

				db = connectionCreator()
			}

			while (db == null) {
				Thread.sleep(50)
				db = availableQueue.poll()
			}
		}

		obtainedQueue.add(db)
		obtainingCount.decrementAndGet()
		return db
	}

	fun release(db: Database) {
		logVerbose("Releasing a connection")

		try { db.enableAutoCommit() }
		catch(e: Throwable) { /*Seems like this database wasn't initialized*/ }

		// Just close the database and forget about it in case the pool is currently resetting,
		// or was reset recently (the fact that the database is not in the obtained-queue means so).
		if (isResetting || !obtainedQueue.remove(db))
			return try { db.close() }
			catch(e: Throwable) {}

		availableQueue.add(db)
	}


	/**
	 * Completely resets the state of the pool, clears all its queues,
	 * closes all non-obtained connectins.
	 *
	 * You may want to reset, for example, in case when your web-server
	 * catches a "com.mysql.jdbc.exceptions.jdbc4.CommunicationsException", which means
	 * that there is a problem with the connection to your MySQL-server (also happens when
	 * MySQL-server was relauched), and all the established connections have become obsolete.
	 * In this situation you'd want to remove all these connections and reset the pool completely.
	 */
	@Synchronized
	fun reset() {
//		if (isResetting) return // Not needed since the method is synchronized
		isResetting = true

		while (obtainingCount.get() > 0) {}

		logInfo("Resetting connection pool")

		// Close and remove all the available databases
		while (availableQueue.isNotEmpty())
			try { availableQueue.poll()?.close() }
			catch(e: Throwable) {}

		// Just remove obtained databases as they may be still in use
		obtainedQueue.clear()

		createdCount.set(0)

		isResetting = false
	}


	inline fun use(fn: (Database)->Unit) {
		val db = obtain()
		try { fn(db) }
		finally { release(db) }
	}

	inline fun <T> useAndGet(fn: (Database)->T): T {
		val db = obtain()
		val res = try { fn(db) }
		finally { release(db) }
		return res
	}
}

package azadev.backt.sql

import azadev.logging.logVerbose
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger


class ConnectionPool(
		val size: Int,
		val connectionCreator: ()->Database
) {
	private val pool = ConcurrentLinkedQueue<Database>()

	private val createdCount = AtomicInteger(0)


	fun obtain(): Database {
		logVerbose("Obtaining a connection")

		var db = pool.poll()
		if (db != null)
			return db

		val c = createdCount.get()
		if (c < size) {
			logVerbose("Creating a new connection")

			if (!createdCount.compareAndSet(c, c+1)) {
				logVerbose("Count changed. Re-obtaining ...")
				return obtain()
			}

			return connectionCreator()
		}

		while (db == null) {
			Thread.sleep(50)
			db = pool.poll()
		}

		return db
	}

	fun release(db: Database) {
		logVerbose("Releasing a connection")

		try { db.enableAutoCommit() }
		catch(e: Throwable) {
			// Seems like this database wasn't initialized
		}

		pool.add(db)
	}

	inline fun use(fn: (Database)->Unit) {
		val db = obtain()
		fn(db)
		release(db)
	}

	inline fun <T> useAndGet(fn: (Database)->T): T {
		val db = obtain()
		val res = fn(db)
		release(db)
		return res
	}
}

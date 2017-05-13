package azadev.backt.sql.test

import azadev.backt.sql.ConnectionPool
import azadev.backt.sql.Database
import org.junit.*
import org.junit.Assert.*
import java.util.*
import kotlin.concurrent.schedule


class ConnectionPoolTest
{
	@Test fun connectionPoolTest() {
		val pool = ConnectionPool(2) { Database() }
		var i = 0

		val db1 = pool.obtain()
		val db2 = pool.obtain()

		Timer().schedule(100L) {
			println(111)
			assertEquals(0, i++)
			pool.release(db1)
			println(11)
		}

		Timer().schedule(200L) {
			println(222)
			assertEquals(2, i++)
			pool.release(db2)
			println(22)
		}

		val db3 = pool.obtain()
		assertEquals(1, i++)
		assertTrue(db1 === db3)

		val db4 = pool.obtain()
		assertEquals(3, i++)
		assertTrue(db2 === db4)
	}
}

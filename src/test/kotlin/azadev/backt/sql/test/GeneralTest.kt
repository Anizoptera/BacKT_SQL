package azadev.backt.sql.test

import azadev.backt.sql.*
import org.junit.*
import org.junit.Assert.*
import java.util.*
import kotlin.concurrent.schedule


class GeneralTest
{
	@Test fun makeValueSetTest() {
		assertEquals("()", makeValueSet())
		assertEquals("(2,1),(2,2),(2,3)", makeValueSet(2, arrayOf(1,2,3)))
		assertEquals("(1,2,3),(4,2,5),(6,2,7)", makeValueSet(listOf(1,4,6), 2, arrayOf(3,5,7)))
	}

	@Test fun escapeTest() {
		assertEquals("", "".escapeSqlLiteral())
		assertEquals("abc", "abc".escapeSqlLiteral())
		assertEquals("a\\'b\\\"c", "a'b\"c".escapeSqlLiteral())
		assertEquals("a\\tb\\nc", "a	b\nc".escapeSqlLiteral())

		assertEquals(null, 'a'.escapeSqlLiteral())
		assertEquals("\\'", '\''.escapeSqlLiteral())
		assertEquals("\\\"", '"'.escapeSqlLiteral())

		assertEquals("", "".escapeSqlIdentifier())
		assertEquals("t", "t".escapeSqlIdentifier())
		assertEquals("table``s_'\"mad/\\ness", "table`s_'\"mad/\\ness".escapeSqlIdentifier())
	}

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

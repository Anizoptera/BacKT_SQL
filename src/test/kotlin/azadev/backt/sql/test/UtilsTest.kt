package azadev.backt.sql.test

import azadev.backt.sql.utils.*
import org.junit.*
import org.junit.Assert.*


class UtilsTest
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
}

package azadev.backt.sql.test

import azadev.backt.sql.QueryBuilder
import org.junit.*
import org.junit.Assert.*


class QueryBuilderTest
{
	@Test fun basics() {
		checkQB("SELECT * FROM `t1`") { select().from("t1") }
		checkQB("SELECT * FROM `table``s_'\"mad/\\ness`") { select().from("table`s_'\"mad/\\ness") }
	}

	@Test fun where() {
		checkQB("SELECT * FROM `t1` WHERE `c1`=123") { select().from("t1").where("c1", 123) }
		checkQB("SELECT * FROM `t1` WHERE `c1`='abc'") { select().from("t1").where("c1", "abc") }
		checkQB("SELECT * FROM `t1` WHERE `c1`=?", listOf(123)) { select().from("t1").wherep("c1", 123) }

		checkQB("SELECT * FROM `t1` WHERE `c1`<>'a\\nb\\'c'") { select().from("t1").whereNot("c1", "a\nb'c") }
		checkQB("SELECT * FROM `t1` WHERE `c1`<>?", listOf("abc")) { select().from("t1").wherepNot("c1", "abc") }

		checkQB("SELECT * FROM `t1` WHERE `c1` IS NULL") { select().from("t1").whereNull("c1") }
		checkQB("SELECT * FROM `t1` WHERE `c1` IS NOT NULL") { select().from("t1").whereNotNull("c1") }

		checkQB("SELECT * FROM `t1` WHERE `c1`>123") { select().from("t1").whereGt("c1", 123) }
		checkQB("SELECT * FROM `t1` WHERE `c1`>?", listOf("abc")) { select().from("t1").wherepGt("c1", "abc") }

		checkQB("SELECT * FROM `t1` WHERE `c1`<123") { select().from("t1").whereLt("c1", 123) }
		checkQB("SELECT * FROM `t1` WHERE `c1`<?", listOf("abc")) { select().from("t1").wherepLt("c1", "abc") }

		checkQB("SELECT * FROM `t1` WHERE `c1` BETWEEN 1 AND 'w'") { select().from("t1").whereBetween("c1", 1, "w") }
		checkQB("SELECT * FROM `t1` WHERE `c1` BETWEEN ? AND ?", listOf(1, "w")) { select().from("t1").wherepBetween("c1", 1, "w") }

		checkQB("SELECT * FROM `t1` WHERE `c1` NOT BETWEEN 1 AND 'w'") { select().from("t1").whereNotBetween("c1", 1, "w") }
		checkQB("SELECT * FROM `t1` WHERE `c1` NOT BETWEEN ? AND ?", listOf(1, "w")) { select().from("t1").wherepNotBetween("c1", 1, "w") }

		checkQB("SELECT * FROM `t1` WHERE `c1` IN (3,'b','acd')") { select().from("t1").whereIn("c1", listOf(3,'b',"acd")) }
		checkQB("SELECT * FROM `t1` WHERE `c1` NOT IN (3,'c\\'de')") { select().from("t1").whereNotIn("c1", listOf(3,null,"c'de")) }
	}

	@Test fun where_severalConditions() {
		checkQB("SELECT * FROM `t1` WHERE `c1`=123 AND `c2`=456") { select().from("t1").where("c1", 123).where("c2", 456) }
		checkQB("SELECT * FROM `t1` WHERE `c1`=? AND `c2`=?", listOf(123, 456)) { select().from("t1").wherep("c1", 123).wherep("c2", 456) }
	}

	private fun checkQB(expected: String, params: List<Any?>? = null, fn: QueryBuilder.()->Unit) {
		val q = QueryBuilder().apply { fn() }

		assertEquals(expected, q.sb.toString())

		if (params != null)
			assertArrayEquals(params.toTypedArray(), q.paramArray)
	}
}

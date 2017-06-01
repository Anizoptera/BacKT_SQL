package azadev.backt.sql.test

import azadev.backt.sql.QueryBuilder
import org.junit.*
import org.junit.Assert.*


class QueryBuilderTest
{
	@Test fun basics() {
		checkQB("SELECT * FROM `t1`") { select().from("t1") }
		checkQB("SELECT * FROM `table``s_'\"mad/\\ness`") { select().from("table`s_'\"mad/\\ness") }

		checkQB("INSERT INTO `t1`") { insert("t1") }

		checkQB("UPDATE `t1`") { update("t1") }

		checkQB("DELETE FROM `t1`") { delete().from("t1") }
	}

	@Test fun set() {
		checkQB("INSERT INTO `t1` SET `c1`=123") { insert("t1").set("c1", 123) }
		checkQB("INSERT INTO `t1` SET `c1`=123, `c2`='abc'") { insert("t1").set("c1", 123).set("c2", "abc") }

		checkQB("INSERT INTO `t1` SET `c1`=?", listOf(123)) { insert("t1").setp("c1", 123) }
		checkQB("INSERT INTO `t1` SET `c1`=123, `c2`=?", listOf("abc")) { insert("t1").set("c1", 123).setp("c2", "abc") }

		checkQB("INSERT INTO `t1` SET `c1`=NULL") { insert("t1").set("c1", null) }

		// TODO: Figure out the way to use functions
//		checkQB("UPDATE `t1` SET `c1`=UNIX_TIMESTAMP()") { update("t1").set("c1", "UNIX_TIMESTAMP()") }
	}

	@Test fun onDuplicateKeyUpdate() {
		checkQB("INSERT INTO `t1` SET `c1`=111, `c2`=? ON DUPLICATE KEY UPDATE `c3`=333, `c4`=?", listOf(222, 444)) {
			insert("t1").set("c1", 111).setp("c2", 222).onDupUpdate("c3", 333).onDupUpdatep("c4", 444)
		}

		checkQB("INSERT INTO `t1` SET `c1`=? ON DUPLICATE KEY UPDATE `c2`=NULL", listOf(111)) {
			insert("t1").setp("c1", 111).onDupUpdate("c2", null)
		}
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

	@Test fun quotingIdentifiers() {
		checkQB("SELECT a.title FROM t1 a INNER JOIN t2 b ON a.id = b.id WHERE c1=123", quoteIdentifiers = false) { select("a.title").from("t1 a INNER JOIN t2 b ON a.id = b.id").where("c1", 123) }
	}

	private fun checkQB(expected: String, params: List<Any?>? = null, quoteIdentifiers: Boolean = true, fn: QueryBuilder.()->Unit) {
		val q = QueryBuilder(quoteIdentifiers = quoteIdentifiers).apply { fn() }

		assertEquals(expected, q.sb.toString())

		assertEquals("Number of params", params?.size ?: 0, q.params?.size ?: 0)

		if (params != null)
			assertArrayEquals(params.toTypedArray(), q.paramArray)
	}
}

# BacKT SQL

This library provides a convenient way to connect with SQL-databases via JDBC, and easily make SQL-requests.

**BacKT SQL** is a part of «**Back-end Kotlin Tool Set**» that consists of:

- [BacKT SQL](https://github.com/Anizoptera/BacKT_SQL) (this one)
- [BacKT WebServer](https://github.com/Anizoptera/BacKT_WebServer) (a Netty-based web-server)

It also uses [KotLog](https://github.com/Anizoptera/Kotlin-Logging-Facade) for logging.

## Installation

First of all, you need a JDBC database connector. Let's say you're using MySQL:

```gradle
dependencies {
	// https://mvnrepository.com/artifact/mysql/mysql-connector-java
	compile "mysql:mysql-connector-java:5.1.42"

	// ...
}
```

Now you need to include **BacKT SQL** into your application. You can easily do it using [git-subrepo](https://github.com/ingydotnet/git-subrepo). Just clone this repo in any subdirectory of your application:

```
git subrepo clone git@github.com:Anizoptera/BacKT_SQL.git <subdir>
```

Add the following into your app's `settings.gradle`:

```gradle
def BKT_SQL = "path/to/BacKT_SQL" // <-- modify this path
include ":BacKT_SQL", ":BacKT_SLF4J"
project(":BacKT_SQL").projectDir = file(BKT_SQL)
project(":BacKT_SLF4J").projectDir = file("$BKT_SQL/modules/BacKT_SLF4J")
```

Add a compile-time dependency in `build.gradle`:

```gradle
compile project(":BacKT_SQL")
```

## Usage

BacKT SQL consists of 4 main parts:

1. `Database`
2. `ConnectionPool`
3. `QueryBuilder`
4. Small utilities

### Database

The main character of the library is the `Database` class. It holds a connection to the database and allows to perform queries and transactions.

```kotlin
// Must be called once, as your application starts:
Database.loadDriver("com.mysql.jdbc.Driver")

val db = Database.connect("jdbc:mysql://localhost:3306/dbname", "user", "pass")
val resultSet = db.executeQuery("SELECT * FROM t WHERE id = ?", 234)
val count = db.executeUpdate("UPDATE t SET id = ?", 234)
```

`Database` provides methods to perform transactions: `disableAutoCommit`, `enableAutoCommit`, `commit`, `rollback`. Here is an example usage:

```kotlin
db.disableAutoCommit()

val rows = db.executeUpdate("UPDATE t SET id = ?", 234)

if (rows == 0) {
	// log error
	db.rollback()
}
else {
	// do something else
	db.commit()
}
```

Read more about transactions on [JDBC Docs](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html).

### ConnectionPool

Another must-have tool is the `ConnectionPool`. It caches database connections so they can be reused for future requests:

```kotlin
val pool = ConnectionPool(maxSize = 50) { Database.connect(url, user, pass) }

// Obtain a connection:
val db = pool.obtain()

// ... do some work ...

// Then release:
pool.release(db)
```

There is a couple of convenience methods named `use` and `useAndGet`. `useAndGet` returns the result of its lambda, `use` doesn't:

```kotlin
pool.use { db ->
	db.executeUpdate(...)
}

val res = pool.useAndGet { db ->
	db.executeQuery(...)
}
```

### QueryBuilder

`QueryBuilder` helps to build and execute SQL-statements. For example, the methods listed below are performing exacly the same request:

```kotlin
fun getUser(db: Database, id: Int, email: String): ResultSet {
	return db.executeQuery(
			"SELECT `name`, `surname` FROM `user` WHERE `id`=$id AND `email`=? LIMIT 1",
			email
	)
}

fun getUser(db: Database, id: Int, email: String): ResultSet {
	return QueryBuilder()
			.select("name", "surname")
			.from("user")
			.where("id", id)
			.wherep("email", email)
			.limit(1)
			.executeQuery(db)
}
```

`QueryBuilder` is an extremely useful helper when it comes to build SQL-quesries that depend on some conditions:

```kotlin
fun getUser(db: Database, id: Int? = null, email: String? = null, order: String? = null, desc: Boolean = false): ResultSet {
	val q = QueryBuilder().select().from("user")

	if (id != null)
		q.where("id", id)

	if (email != null)
		q.wherep("email", email)

	if (order != null)
		q.orderBy(order, desc)

	return q.executeQuery(db)
}
```

### Utilities

`count`, `countByte`, `countLong`, `countFloat`, `countDouble`:

```kotlin
val countInt = db.executeUpdate("UPDATE ...").count
val idLong = db.executeQuery("SELECT id FROM ...").countLong
val numFloat = db.executeQuery("SELECT rating FROM ...").countFloat
```

`single`, `toList`:

```kotlin
class User(res: ResultSet) { ... }

val user = db.executeQuery("SELECT * FROM user LIMIT 1").single(::User)
val userList = db.executeQuery("SELECT * FROM user").toList(::User)
```

... and others: `escapeSqlLiteral`, `escapeSqlIdentifier`, `makeValueSet`, `asParameterized`

## License

This software is released under the MIT License.
See [LICENSE.txt](LICENSE.txt) for details.

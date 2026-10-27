package com.example.daftar.backend

import com.example.daftar.backend.localDb.MigrationSql
import com.example.daftar.backend.localDb.PAGE_BALANCES_SQL
import java.sql.Connection
import java.sql.DriverManager
import org.junit.Assert.*
import org.junit.Test

/** Executes the same SQL as Room against real SQLite, without requiring a device. */
class DatabaseMigrationTest {
    private fun database(test: (Connection) -> Unit) {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { db ->
            db.exec("PRAGMA foreign_keys = ON")
            test(db)
        }
    }

    @Test fun versionOnePreservesCustomersAndAllEntries() = database { db ->
        db.exec("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, number TEXT NOT NULL, credit INTEGER NOT NULL, isPayed INTEGER NOT NULL)")
        db.exec("INSERT INTO transactions VALUES (1, 'Ahmad', '123', 100, 0), (2, 'Ahmad', '123', 30, 1), (3, 'Sara', '456', 50, 0)")
        MigrationSql.FROM_1_TO_2.forEach { db.exec(it) }
        MigrationSql.FROM_3_TO_4.forEach { db.exec(it) }
        assertEquals(3L, db.scalar("SELECT COUNT(*) FROM transactions"))
        assertEquals(2L, db.scalar("SELECT COUNT(*) FROM Pages"))
        assertEquals(70L, balance(db, "Ahmad"))
        assertEquals(50L, balance(db, "Sara"))
        assertEquals(0L, db.scalar("SELECT COUNT(*) FROM pragma_foreign_key_check"))
    }

    @Test fun versionThreeRecoversOrphansWithoutLosingTransactions() = database { db ->
        createVersionTwoOrThree(db)
        db.exec("INSERT INTO Pages VALUES (1, 'Ahmad', '123', 999)")
        db.exec("INSERT INTO transactions VALUES (1, 'Item', 100, 0, 1), (2, 'Orphan', 25, 0, 42)")
        MigrationSql.FROM_3_TO_4.forEach { db.exec(it) }
        assertEquals(2L, db.scalar("SELECT COUNT(*) FROM transactions"))
        assertEquals(100L, balance(db, "Ahmad")) // ignores stale stored credit
        assertEquals(25L, balance(db, "Recovered page 42"))
        assertEquals(0L, db.scalar("SELECT COUNT(*) FROM pragma_foreign_key_check"))
    }

    @Test fun migratedForeignKeyCascadesAndRejectsOrphans() = database { db ->
        createVersionTwoOrThree(db)
        db.exec("INSERT INTO Pages VALUES (1, 'Ahmad', '', 0)")
        db.exec("INSERT INTO transactions VALUES (1, 'Item', 100, 0, 1)")
        MigrationSql.FROM_3_TO_4.forEach { db.exec(it) }
        db.exec("DELETE FROM Pages WHERE id = 1")
        assertEquals(0L, db.scalar("SELECT COUNT(*) FROM transactions"))
        try {
            db.exec("INSERT INTO transactions VALUES (2, 'Orphan', 1, 0, 99)")
            fail("Foreign key allowed an orphan")
        } catch (_: java.sql.SQLException) { /* expected */ }
    }

    @Test fun aggregateSupportsCreditLargeTotalsAndEmptyPages() = database { db ->
        createVersionTwoOrThree(db)
        db.exec("INSERT INTO Pages VALUES (1, 'Credit', '', 999), (2, 'Large', '', 999), (3, 'Empty', '', 999)")
        db.exec("INSERT INTO transactions VALUES (1, 'Debt', 100, 0, 1), (2, 'Payment', 150, 1, 1), (3, 'Item', 2147483647, 0, 2), (4, 'Item', 2147483647, 0, 2)")
        MigrationSql.FROM_3_TO_4.forEach { db.exec(it) }
        assertEquals(-50L, balance(db, "Credit"))
        assertEquals(4294967294L, balance(db, "Large"))
        assertEquals(0L, balance(db, "Empty"))
        db.exec("DELETE FROM transactions WHERE id = 2")
        assertEquals(100L, balance(db, "Credit"))
    }

    private fun createVersionTwoOrThree(db: Connection) {
        db.exec("CREATE TABLE Pages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, number TEXT NOT NULL, credit INTEGER NOT NULL)")
        db.exec("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, details TEXT NOT NULL, credit INTEGER NOT NULL, isPayed INTEGER NOT NULL, pageId INTEGER NOT NULL)")
    }

    private fun balance(db: Connection, name: String): Long {
        db.createStatement().use { statement ->
            statement.executeQuery(PAGE_BALANCES_SQL).use { rows ->
                while (rows.next()) if (rows.getString("name") == name) return rows.getLong("credit")
            }
        }
        error("Page not found: $name")
    }

    private fun Connection.exec(sql: String) { createStatement().use { it.execute(sql) } }
    private fun Connection.scalar(sql: String): Long = createStatement().use { statement ->
        statement.executeQuery(sql).use { rows -> check(rows.next()); rows.getLong(1) }
    }
}

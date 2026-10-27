package com.example.daftar.backend.localDb

/** SQL shared by Android migrations and host-side SQLite regression tests. */
object MigrationSql {
    val FROM_1_TO_2 = listOf(
        "CREATE TABLE IF NOT EXISTS Pages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, number TEXT NOT NULL, credit INTEGER NOT NULL)",
        "INSERT INTO Pages (name, number, credit) SELECT name, number, 0 FROM transactions GROUP BY name, number",
        "CREATE TABLE transactions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, details TEXT NOT NULL, credit INTEGER NOT NULL, isPayed INTEGER NOT NULL, pageId INTEGER NOT NULL)",
        "INSERT INTO transactions_new SELECT t.id, t.name, t.credit, t.isPayed, p.id FROM transactions t JOIN Pages p ON p.name = t.name AND p.number = t.number",
        "DROP TABLE transactions",
        "ALTER TABLE transactions_new RENAME TO transactions",
    )
    val FROM_3_TO_4 = listOf(
        "INSERT INTO Pages (id, name, number, credit) SELECT DISTINCT t.pageId, 'Recovered page ' || t.pageId, '', 0 FROM transactions t WHERE NOT EXISTS (SELECT 1 FROM Pages p WHERE p.id = t.pageId)",
        "CREATE TABLE transactions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, details TEXT NOT NULL, credit INTEGER NOT NULL, isPayed INTEGER NOT NULL, pageId INTEGER NOT NULL, FOREIGN KEY(pageId) REFERENCES Pages(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
        "INSERT INTO transactions_new SELECT id, details, credit, isPayed, pageId FROM transactions",
        "DROP TABLE transactions",
        "ALTER TABLE transactions_new RENAME TO transactions",
        "CREATE INDEX index_transactions_pageId ON transactions(pageId)",
    )
}

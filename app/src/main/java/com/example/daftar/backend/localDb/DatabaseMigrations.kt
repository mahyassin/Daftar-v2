package com.example.daftar.backend.localDb

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private fun migration(from: Int, to: Int, statements: List<String>) = object : Migration(from, to) {
        override fun migrate(db: SupportSQLiteDatabase) {
            statements.forEach { db.execSQL(it) }
        }
    }

    val FROM_1_TO_2 = migration(1, 2, MigrationSql.FROM_1_TO_2)
    // The version-3 source retained the version-2 entity columns.
    val FROM_2_TO_3 = migration(2, 3, emptyList())
    val FROM_3_TO_4 = migration(3, 4, MigrationSql.FROM_3_TO_4)
    val ALL = arrayOf(FROM_1_TO_2, FROM_2_TO_3, FROM_3_TO_4)
}

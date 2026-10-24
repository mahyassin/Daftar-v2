package com.example.daftar.backend.localDb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Transaction::class, Pages:: class],
    version = 3,
    exportSchema = true,
)
abstract class LocalDB: RoomDatabase(){
    abstract fun getTransactionsDao(): TransactionDao
    abstract fun getPageDao(): PageDao
}
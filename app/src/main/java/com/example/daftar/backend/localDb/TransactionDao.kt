package com.example.daftar.backend.localDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction as Atomic
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao {
    @Query("SELECT * FROM transactions WHERE pageId = :pageId ORDER BY id DESC")
    abstract fun observeTransaction(pageId: Int): Flow<List<Transaction>>

    @Insert
    abstract suspend fun insert(transaction: Transaction): Long

    @Delete
    abstract suspend fun delete(transaction: Transaction)

    @Query("SELECT COALESCE(SUM(CASE WHEN isPayed THEN -CAST(credit AS INTEGER) ELSE credit END), 0) FROM transactions WHERE pageId = :pageId")
    abstract suspend fun balance(pageId: Int): Long

    /** Read and insert atomically so repeated settlement cannot pay twice. */
    @Atomic
    open suspend fun settlePage(pageId: Int) {
        var remaining = balance(pageId)
        while (remaining > 0) {
            val amount = minOf(remaining, Int.MAX_VALUE.toLong()).toInt()
            insert(Transaction(details = "Balance payment", credit = amount, isPayed = true, pageId = pageId))
            remaining -= amount
        }
    }
}

@Dao
interface PageDao {
    @Query(PAGE_BALANCES_SQL)
    fun observePages(): Flow<List<PageBalance>>

    @Insert
    suspend fun insert(page: Pages): Long

    @Query("DELETE FROM Pages WHERE id = :id")
    suspend fun deleteById(id: Int)
}

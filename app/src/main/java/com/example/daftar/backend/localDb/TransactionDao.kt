package com.example.daftar.backend.localDb

import android.graphics.pdf.PdfDocument
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun observeTransaction(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(name: Transaction)

    @Delete
    suspend fun delete(name: Transaction)

    @Query("Select * From transactions where pageId = :id")
    fun getTransactionsForPage(id: Int): Flow<List<Transaction>>

}

@Dao
interface PageDao{

    @Query("SELECT * FROM Pages ORDER BY id DESC")
    fun observePages(): Flow<List<Pages>>

    @Insert
    suspend fun insert(name: Pages)

    @Delete
    suspend fun delete(name: Pages)

    @Query("Delete from pages where id = :id")
    suspend fun deleteById(id: Int)

    @Query("Select  * from pages where id = :id")
    fun getPage(id: Int): Pages
}
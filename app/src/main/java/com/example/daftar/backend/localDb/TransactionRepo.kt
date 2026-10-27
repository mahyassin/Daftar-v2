package com.example.daftar.backend.localDb

import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {
    fun observeTransaction(pageId: Int): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun settlePage(pageId: Int)
}

class TransactionRepo(private val dao: TransactionDao) : TransactionsRepository {
    override fun observeTransaction(pageId: Int) = dao.observeTransaction(pageId)
    override suspend fun insertTransaction(transaction: Transaction) {
        require(transaction.details.isNotBlank() && transaction.credit > 0)
        dao.insert(transaction)
    }
    override suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)
    override suspend fun settlePage(pageId: Int) = dao.settlePage(pageId)
}

interface PagesRepository {
    fun observePages(): Flow<List<PageBalance>>
    suspend fun savePage(page: Pages)
    suspend fun deleteById(id: Int)
}

class PagesRepo(private val dao: PageDao) : PagesRepository {
    override fun observePages() = dao.observePages()
    override suspend fun savePage(page: Pages) {
        require(page.name.isNotBlank())
        dao.insert(page)
    }
    override suspend fun deleteById(id: Int) = dao.deleteById(id)
}

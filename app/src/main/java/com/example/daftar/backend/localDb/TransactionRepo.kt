package com.example.daftar.backend.localDb

import kotlinx.coroutines.flow.Flow

class TransactionRepo(private val dao: TransactionDao) {

    fun observeTransaction(): Flow<List<Transaction>> {
        return dao.observeTransaction()
    }

    suspend fun insertTransaction(transaction: Transaction){
        dao.insert(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction){
        dao.delete(transaction)
    }

    fun getTransactionsForPage(id: Int){
        dao.getTransactionsForPage(id)
    }

}

class PagesRepo(private val dao: PageDao){

    fun observePages(): Flow<List<Pages>>{
        return dao.observePages()
    }

    fun getPageById(id: Int): Pages{
        return dao.getPage(id)
    }

    suspend fun savePage(page: Pages){
        dao.insert(page)
    }

    suspend fun delete(page: Pages){
        dao.delete(page)
    }

    suspend fun deleteById(id: Int){
        dao.deleteById(id)
    }
}
package com.example.daftar

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.daftar.backend.localDb.LocalDB
import com.example.daftar.backend.localDb.PagesRepo
import com.example.daftar.backend.localDb.TransactionRepo
import com.example.daftar.ui.screens.home.HomeViewModel
import com.example.daftar.ui.screens.pageDetails.PageDetailsViewModel

class AppContainer(context: Context) {

    private  val appContext = context.applicationContext

    private  val database: LocalDB by lazy {
        Room.databaseBuilder(
            appContext,
            LocalDB::class.java,
            "transactions.db"
        ).build()
    }

    private val transactionRepo: TransactionRepo by lazy {
        TransactionRepo(database.getTransactionsDao())
    }

    private val pagesReepo: PagesRepo by lazy {
        PagesRepo(database.getPageDao())
    }

    val factory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer { HomeViewModel(pagesReepo) }
            initializer { PageDetailsViewModel(transactionRepo) }
        }
    }
}
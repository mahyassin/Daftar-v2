package com.example.daftar.ui.screens.pageDetails

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daftar.backend.localDb.Transaction
import com.example.daftar.backend.localDb.TransactionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PageDetailsViewModel(private val repo: TransactionRepo, private val pageId: Int): ViewModel() {

    val transactions = repo.observeTransaction(pageId).stateIn(
        initialValue = emptyList<Transaction>(),
        started = SharingStarted.WhileSubscribed(5000),
        scope = viewModelScope
    )
    private var _uiState = MutableStateFlow (DetailsPageUiState())
    val uiState = _uiState.asStateFlow()

    fun eventHandler(event: DetailsEvents){
        when(event){
            is DetailsEvents.AddItem -> { insertTransaction(event.itemName, event.itemPrice) }
            DetailsEvents.DissmissDialog -> _uiState.update { it.copy(isDialogOpen = false) }
            is DetailsEvents.NameTextFieldChange -> _uiState.update { it.copy(nameTextField = event.value) }
            is DetailsEvents.PriceTextFieldChange -> _uiState.update{ it.copy(priceField = event.value)}
            DetailsEvents.OnAddButtonPress -> _uiState.update { it.copy(isDialogOpen = true) }
            is DetailsEvents.UpdateTab -> _uiState.update { it.copy(tabs = event.tab) }
            is DetailsEvents.DeleteTransaction -> deleteTransaction(event.id)
            is DetailsEvents.UpdateTransaction -> updateTransaction(event.id)
        }
    }

    fun insertTransaction(itemName: String, itemPrice: String){
        val credit = if(itemPrice.isDigitsOnly()) itemPrice.toInt() else null
        if (credit == null) return
        val transaction = Transaction(0, itemName, credit, false, pageId)
        viewModelScope.launch {
            repo.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Int){
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }

    fun updateTransaction(id: Int){
        viewModelScope.launch {
            val t = repo.getTransactionsForPage(id)
            repo.updateTransaction(t)
        }
    }

}
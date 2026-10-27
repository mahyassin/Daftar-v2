package com.example.daftar.ui.screens.pageDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daftar.backend.parseAmount
import com.example.daftar.backend.localDb.Transaction
import com.example.daftar.backend.localDb.TransactionsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PageDetailsViewModel(private val repo: TransactionsRepository, private val pageId: Int) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailsPageUiState())
    val uiState = _uiState.asStateFlow()
    val transactions = repo.observeTransaction(pageId)
        .onEach { _uiState.update { it.copy(isLoading = false) } }
        .catch { error ->
            if (error is CancellationException) throw error
            _uiState.update { it.copy(isLoading = false, error = "Could not load transactions. Please reopen this page.") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun eventHandler(event: DetailsEvents) {
        if (_uiState.value.isSaving) return
        when (event) {
            DetailsEvents.OnAddButtonPress -> _uiState.update {
                it.copy(isDialogOpen = true, nameTextField = "", priceField = "", tabs = DetailsTabs.Debt, error = null)
            }
            DetailsEvents.DissmissDialog -> _uiState.update { it.copy(isDialogOpen = false, error = null) }
            is DetailsEvents.NameTextFieldChange -> _uiState.update { it.copy(nameTextField = event.value, error = null) }
            is DetailsEvents.PriceTextFieldChange -> _uiState.update { it.copy(priceField = event.value, error = null) }
            is DetailsEvents.UpdateTab -> _uiState.update { it.copy(tabs = event.tab, error = null) }
            is DetailsEvents.AddItem -> insertTransaction(event.itemName, event.itemPrice)
            is DetailsEvents.DeleteItem -> {
                if (event.item.pageId == pageId) write("Could not delete this entry. Please try again.") {
                    repo.deleteTransaction(event.item)
                }
            }
            DetailsEvents.SettleBalance -> write("Could not record the payment. Please try again.") {
                repo.settlePage(pageId)
            }
        }
    }

    private fun insertTransaction(name: String, price: String) {
        val amount = parseAmount(price)
        if (name.isBlank() || name.trim().length > 200 || amount == null) {
            _uiState.update { it.copy(error = "Enter a description (up to 200 characters) and a whole amount from 1 to 2,147,483,647 ₪.") }
            return
        }
        val payment = _uiState.value.tabs == DetailsTabs.Payment
        write("Could not save this entry. Please try again.") {
            repo.insertTransaction(Transaction(details = name.trim(), credit = amount, isPayed = payment, pageId = pageId))
            _uiState.update { it.copy(isDialogOpen = false, nameTextField = "", priceField = "", tabs = DetailsTabs.Debt) }
        }
    }

    private fun write(message: String, action: suspend () -> Unit) {
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _uiState.update { it.copy(error = message) } }
            finally { _uiState.update { it.copy(isSaving = false) } }
        }
    }
}

enum class DetailsTabs { Debt, Payment }

data class DetailsPageUiState(
    val isDialogOpen: Boolean = false,
    val nameTextField: String = "",
    val priceField: String = "",
    val tabs: DetailsTabs = DetailsTabs.Debt,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface DetailsEvents {
    data object OnAddButtonPress : DetailsEvents
    data object DissmissDialog : DetailsEvents
    data class PriceTextFieldChange(val value: String) : DetailsEvents
    data class NameTextFieldChange(val value: String) : DetailsEvents
    data class UpdateTab(val tab: DetailsTabs) : DetailsEvents
    data class AddItem(val itemName: String, val itemPrice: String) : DetailsEvents
    data class DeleteItem(val item: Transaction) : DetailsEvents
    data object SettleBalance : DetailsEvents
}

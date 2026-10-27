package com.example.daftar.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daftar.backend.localDb.Pages
import com.example.daftar.backend.localDb.PagesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: PagesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    val pages = repo.observePages()
        .onEach { _uiState.update { it.copy(isLoading = false) } }
        .catch { error ->
            if (error is CancellationException) throw error
            _uiState.update { it.copy(isLoading = false, error = "Could not load pages. Please reopen this screen.") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun handleHomeEvent(event: HomeEvents) {
        if (_uiState.value.isSaving) return
        when (event) {
            HomeEvents.OnActionButtonPressed -> _uiState.update {
                it.copy(isDialogOpen = true, nameTextField = "", numberTextField = "", error = null)
            }
            HomeEvents.DissmisDialog -> _uiState.update { it.copy(isDialogOpen = false, error = null) }
            is HomeEvents.OnNameTextFieldChange -> _uiState.update { it.copy(nameTextField = event.name, error = null) }
            is HomeEvents.OnNumberTextFieldChange -> _uiState.update { it.copy(numberTextField = event.number, error = null) }
            is HomeEvents.SelectTab -> _uiState.update { it.copy(tab = event.tab) }
            is HomeEvents.AddPageEvent -> savePage(event.name, event.number)
            is HomeEvents.DeletePage -> write("Could not delete this page. Please try again.") {
                repo.deleteById(event.id)
            }
        }
    }

    private fun savePage(name: String, number: String) {
        if (name.isBlank() || name.trim().length > 120 || number.trim().length > 40) {
            _uiState.update { it.copy(error = "Enter a name (up to 120 characters) and a number up to 40 characters.") }
            return
        }
        write("Could not save this page. Please try again.") {
            repo.savePage(Pages(name = name.trim(), number = number.trim(), credit = 0))
            _uiState.update { it.copy(isDialogOpen = false, nameTextField = "", numberTextField = "") }
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

enum class HomeTab { Pages, Overview }

data class HomeUiState(
    val tab: HomeTab = HomeTab.Pages,
    val isDialogOpen: Boolean = false,
    val nameTextField: String = "",
    val numberTextField: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeEvents {
    data object OnActionButtonPressed : HomeEvents
    data object DissmisDialog : HomeEvents
    data class OnNameTextFieldChange(val name: String) : HomeEvents
    data class OnNumberTextFieldChange(val number: String) : HomeEvents
    data class AddPageEvent(val name: String, val number: String) : HomeEvents
    data class DeletePage(val id: Int) : HomeEvents
    data class SelectTab(val tab: HomeTab) : HomeEvents
}

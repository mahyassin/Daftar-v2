package com.example.daftar.ui.screens.home

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daftar.backend.localDb.Pages
import com.example.daftar.backend.localDb.PagesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private  val repo: PagesRepo): ViewModel() {
    val uiState = MutableStateFlow(HomeUiState())
    private var _uiState = uiState.asStateFlow()

    val pages = repo.observePages().stateIn(
        viewModelScope,
        initialValue =  emptyList<Pages>(),
        started = SharingStarted.WhileSubscribed(5000)
    )



    fun handleHomeEvent(event: HomeEvents){
        when(event){
            is HomeEvents.OnActionButtonPressed -> {uiState.update { it.copy(isDialogOpen = true) }}
            is HomeEvents.AddPageEvent -> { savePage(event.name, event.number) }
            is HomeEvents.DissmisDialog -> { uiState.update { it.copy(isDialogOpen = false) } }
            is HomeEvents.OnNameTextFieldChange -> { uiState.update { it.copy(nameTextField = event.name) } }
            is HomeEvents.OnNumberTextFieldChange -> { updateNumberField(event.number) }
            is HomeEvents.DeletePage -> deletePage(event.id)
            is HomeEvents.NavigateToDetails -> {}
        }
    }

    fun updateNumberField(value: String){
        if (!value.isDigitsOnly()) return
        uiState.update { it.copy(numberTextField = value) }
    }


    fun savePage(name: String, number: String){
        viewModelScope.launch {
            repo.savePage(Pages(0, name, number, 0))
        }
    }

    fun deletePage(id: Int){
        viewModelScope.launch {
            repo.deleteById(id)
        }
    }


}
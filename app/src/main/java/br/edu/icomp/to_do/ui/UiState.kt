package br.edu.icomp.to_do.ui

import br.edu.icomp.to_do.model.TodoTask

sealed class UiState {
    object Loading : UiState()
    object Empty : UiState()
    data class Success(val tasks: List<TodoTask>) : UiState()
    data class Error(val message: String) : UiState()
}
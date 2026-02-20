package br.edu.icomp.to_do.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.icomp.to_do.data.TaskRepository
import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, PENDING, DONE }

class TodoViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // ---------- FILTRO ----------
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    // ---------- UNDO ----------
    private var lastDeleted: TodoTask? = null

    // ---------- UI STATE (reativo) ----------
    val uiState: StateFlow<UiState> =
        combine(repository.observeAll(), _filter) { tasks, filter ->

            val filtered = when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.PENDING -> tasks.filter { !it.done }
                TaskFilter.DONE -> tasks.filter { it.done }
            }

            when {
                filtered.isEmpty() -> UiState.Empty
                else -> UiState.Success(filtered)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    // ---------- AÇÕES ----------

    fun updateFilter(newFilter: TaskFilter) {
        _filter.value = newFilter
    }

    fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            repository.add(trimmed)
        }
    }

    fun toggleDone(task: TodoTask) {
        viewModelScope.launch {
            repository.toggleDone(task)
        }
    }

    fun deleteTask(task: TodoTask) {
        lastDeleted = task
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun undoDelete() {
        val task = lastDeleted ?: return
        lastDeleted = null

        viewModelScope.launch {
            repository.add(task.title)
        }
    }
}

/**
 * Factory para criar o ViewModel passando o Repository.
 * Necessário porque agora o ViewModel tem parâmetro no construtor.
 */
class TodoViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(repository) as T
    }
}
package br.edu.icomp.to_do.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.icomp.to_do.data.TaskRepositoryContract
import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, PENDING, DONE }

class TodoViewModel(
    private val repository: TaskRepositoryContract
) : ViewModel() {

    // ---------- FILTRO ----------
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    // ---------- BUSCA ----------
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // ---------- UNDO ----------
    private var lastDeleted: TodoTask? = null

    // ---------- UI STATE (reativo) ----------
    val uiState: StateFlow<UiState> =
        combine(
            repository.observeAll(),
            _filter,
            _query.debounce(300).distinctUntilChanged()
        ) { tasks, filter, query ->

            val q = query.trim()

            val byStatus = when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.PENDING -> tasks.filter { !it.done }
                TaskFilter.DONE -> tasks.filter { it.done }
            }

            val bySearch =
                if (q.isBlank()) byStatus
                else byStatus.filter { it.title.contains(q, ignoreCase = true) }

            when {
                bySearch.isEmpty() -> UiState.Empty
                else -> UiState.Success(bySearch)
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

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
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

class TodoViewModelFactory(
    private val repository: TaskRepositoryContract
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(repository) as T
    }
}
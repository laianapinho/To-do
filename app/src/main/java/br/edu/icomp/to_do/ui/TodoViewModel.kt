package br.edu.icomp.to_do.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.edu.icomp.to_do.model.TodoTask

enum class TaskFilter { ALL, PENDING, DONE }

class TodoViewModel : ViewModel() {

    var tasks by mutableStateOf(listOf<TodoTask>())
        private set

    var filter by mutableStateOf(TaskFilter.ALL)
        private set

    private var nextId = 1

    fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return

        val newTask = TodoTask(
            id = nextId++,
            title = trimmed,
            done = false
        )

        tasks = listOf(newTask) + tasks
    }

    fun toggleDone(taskId: Int) {
        tasks = tasks.map { t ->
            if (t.id == taskId) t.copy(done = !t.done) else t
        }
    }

    fun deleteTask(taskId: Int) {
        tasks = tasks.filterNot { it.id == taskId }
    }

    fun updateFilter(newFilter: TaskFilter) {
        filter = newFilter
    }

    fun filteredTasks(): List<TodoTask> {
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.done }
            TaskFilter.DONE -> tasks.filter { it.done }
        }
    }
}

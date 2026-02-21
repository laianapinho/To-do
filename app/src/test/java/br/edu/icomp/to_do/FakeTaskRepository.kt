package br.edu.icomp.to_do

import br.edu.icomp.to_do.data.TaskRepositoryContract
import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTaskRepository : TaskRepositoryContract {

    private val _tasks = MutableStateFlow<List<TodoTask>>(emptyList())
    private var nextId = 1

    override fun observeAll(): Flow<List<TodoTask>> = _tasks.asStateFlow()

    override suspend fun add(title: String) {
        val t = TodoTask(id = nextId++, title = title, done = false)
        _tasks.value = listOf(t) + _tasks.value
    }

    override suspend fun toggleDone(task: TodoTask) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) it.copy(done = !it.done) else it
        }
    }

    override suspend fun delete(task: TodoTask) {
        _tasks.value = _tasks.value.filterNot { it.id == task.id }
    }

    fun seed(vararg tasks: TodoTask) {
        nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
        _tasks.value = tasks.toList()
    }
}
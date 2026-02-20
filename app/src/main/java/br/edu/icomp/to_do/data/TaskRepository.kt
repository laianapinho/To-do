package br.edu.icomp.to_do.data

import br.edu.icomp.to_do.data.local.TaskDao
import br.edu.icomp.to_do.data.local.TaskEntity
import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(
    private val dao: TaskDao
) {
    fun observeAll(): Flow<List<TodoTask>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    suspend fun add(title: String) {
        dao.insert(TaskEntity(title = title, done = false))
    }

    suspend fun toggleDone(task: TodoTask) {
        dao.update(TaskEntity(id = task.id, title = task.title, done = !task.done))
    }

    suspend fun delete(task: TodoTask) {
        dao.delete(TaskEntity(id = task.id, title = task.title, done = task.done))
    }
}

private fun TaskEntity.toDomain(): TodoTask =
    TodoTask(id = id, title = title, done = done)
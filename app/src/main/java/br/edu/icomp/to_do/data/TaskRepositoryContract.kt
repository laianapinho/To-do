package br.edu.icomp.to_do.data

import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.flow.Flow

interface TaskRepositoryContract {
    fun observeAll(): Flow<List<TodoTask>>
    suspend fun add(title: String)
    suspend fun toggleDone(task: TodoTask)
    suspend fun delete(task: TodoTask)
}
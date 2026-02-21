package br.edu.icomp.to_do

import br.edu.icomp.to_do.model.TodoTask
import br.edu.icomp.to_do.ui.TaskFilter
import br.edu.icomp.to_do.ui.TodoViewModel
import br.edu.icomp.to_do.ui.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `quando iniciar sem tarefas, estado deve ser Empty`() = runTest {
        val repo = FakeTaskRepository()
        val vm = TodoViewModel(repo)

        runCurrent() // processa o stateIn
        val s = vm.uiState.value
        assertEquals(true, s is UiState.Empty || s is UiState.Loading)
        // Se vier Loading aqui, roda mais um ciclo:
        runCurrent()
        assertEquals(UiState.Empty, vm.uiState.value)
    }

    @Test
    fun `addTask adiciona tarefa e estado vira Success`() = runTest {
        val repo = FakeTaskRepository()
        val vm = TodoViewModel(repo)

        runCurrent()

        vm.addTask("Estudar Kotlin")
        runCurrent()

        val s = vm.uiState.value as UiState.Success
        assertEquals(1, s.tasks.size)
        assertEquals("Estudar Kotlin", s.tasks[0].title)
    }

    @Test
    fun `filtro DONE mostra apenas concluidas`() = runTest {
        val repo = FakeTaskRepository()
        repo.seed(
            TodoTask(1, "A", done = false),
            TodoTask(2, "B", done = true),
            TodoTask(3, "C", done = true)
        )
        val vm = TodoViewModel(repo)

        runCurrent()
        vm.updateFilter(TaskFilter.DONE)
        runCurrent()

        val s = vm.uiState.value as UiState.Success
        assertEquals(listOf("B", "C"), s.tasks.map { it.title })
    }

    @Test
    fun `busca filtra por texto`() = runTest {
        val repo = FakeTaskRepository()
        repo.seed(
            TodoTask(1, "Estudar Compose", done = false),
            TodoTask(2, "Ir na academia", done = false),
            TodoTask(3, "Estudar Room", done = false)
        )
        val vm = TodoViewModel(repo)

        runCurrent()

        vm.updateQuery("estudar")
        // seu VM tem debounce(300)
        advanceTimeBy(350)
        runCurrent()

        val s = vm.uiState.value as UiState.Success
        assertEquals(listOf("Estudar Compose", "Estudar Room"), s.tasks.map { it.title })
    }

    @Test
    fun `delete + undoDelete restaura tarefa`() = runTest {
        val repo = FakeTaskRepository()
        repo.seed(TodoTask(1, "X", done = false))
        val vm = TodoViewModel(repo)

        runCurrent()
        val before = vm.uiState.value as UiState.Success
        val task = before.tasks[0]

        vm.deleteTask(task)
        runCurrent()
        assertEquals(UiState.Empty, vm.uiState.value)

        vm.undoDelete()
        runCurrent()

        val after = vm.uiState.value as UiState.Success
        assertEquals(1, after.tasks.size)
        assertEquals("X", after.tasks[0].title)
    }
}
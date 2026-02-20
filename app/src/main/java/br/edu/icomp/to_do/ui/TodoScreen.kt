package br.edu.icomp.to_do.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.edu.icomp.to_do.model.TodoTask
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }

    val state by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "To-Do", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            // Input + Add
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nova tarefa") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addTask(input)
                        input = ""
                    }
                ) { Text("Add") }
            }

            Spacer(Modifier.height(12.dp))

            // Filtros
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter == TaskFilter.ALL,
                    onClick = { viewModel.updateFilter(TaskFilter.ALL) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = currentFilter == TaskFilter.PENDING,
                    onClick = { viewModel.updateFilter(TaskFilter.PENDING) },
                    label = { Text("Pendentes") }
                )
                FilterChip(
                    selected = currentFilter == TaskFilter.DONE,
                    onClick = { viewModel.updateFilter(TaskFilter.DONE) },
                    label = { Text("Concluídas") }
                )
            }

            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                is UiState.Loading -> Text("Carregando...")
                is UiState.Empty -> Text("Nenhuma tarefa ainda 🙂")
                is UiState.Error -> Text("Erro: ${s.message}")

                is UiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.tasks, key = { it.id }) { task ->

                            val dismissState = rememberDismissState(
                                confirmStateChange = { value ->
                                    if (value == DismissValue.DismissedToStart) {
                                        // deletar + snackbar undo
                                        viewModel.deleteTask(task)

                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Tarefa excluída",
                                                actionLabel = "Desfazer",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete()
                                            }
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                background = {
                                    // fundo ao arrastar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                    }
                                },
                                dismissContent = {
                                    TaskRow(
                                        title = task.title,
                                        done = task.done,
                                        onToggle = { viewModel.toggleDone(task) },
                                        onDelete = {
                                            viewModel.deleteTask(task)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Tarefa excluída",
                                                    actionLabel = "Desfazer",
                                                    withDismissAction = true,
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.undoDelete()
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    title: String,
    done: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = done,
                onCheckedChange = { onToggle() }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
            }
        }
    }
}
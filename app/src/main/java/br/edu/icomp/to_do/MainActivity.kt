package br.edu.icomp.to_do

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import br.edu.icomp.to_do.data.TaskRepository
import br.edu.icomp.to_do.data.local.AppDatabase
import br.edu.icomp.to_do.ui.TodoScreen
import br.edu.icomp.to_do.ui.TodoViewModel
import br.edu.icomp.to_do.ui.TodoViewModelFactory
import br.edu.icomp.to_do.ui.theme.TODOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "todo.db"
        ).build()

        val repository = TaskRepository(db.taskDao())
        val factory = TodoViewModelFactory(repository)

        setContent {
            TODOTheme {
                val vm: TodoViewModel = viewModel(factory = factory)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoScreen(
                        viewModel = vm,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
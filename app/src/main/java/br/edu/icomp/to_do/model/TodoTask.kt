package br.edu.icomp.to_do.model

data class TodoTask(
    val id: Int,
    val title: String,
    val done: Boolean = false
)

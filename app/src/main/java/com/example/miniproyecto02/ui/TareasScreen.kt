package com.example.miniproyecto02.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miniproyecto02.data.Tarea
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))

private fun formatearFecha(milisegundos: Long): String = formatoFecha.format(Date(milisegundos))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(viewModel: TareasViewModel = viewModel()) {
    val tareas by viewModel.tareas.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val orden by viewModel.orden.collectAsState()
    val pendientes by viewModel.pendientes.collectAsState()
    val completadas by viewModel.completadas.collectAsState()

    var menuFiltroAbierto by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaEnEdicion by remember { mutableStateOf<Tarea?>(null) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis tareas")
                        Text(
                            text = "$pendientes pendientes",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Menú de filtrado
                    Box {
                        IconButton(onClick = { menuFiltroAbierto = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filtrar tareas")
                        }
                        DropdownMenu(
                            expanded = menuFiltroAbierto,
                            onDismissRequest = { menuFiltroAbierto = false }
                        ) {
                            FiltroTareas.entries.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.etiqueta) },
                                    onClick = {
                                        viewModel.cambiarFiltro(opcion)
                                        menuFiltroAbierto = false
                                    },
                                    leadingIcon = {
                                        if (opcion == filtro) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Menú de opciones: ordenar y borrar completadas
                    Box {
                        IconButton(onClick = { menuOpcionesAbierto = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = menuOpcionesAbierto,
                            onDismissRequest = { menuOpcionesAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Ordenar por", style = MaterialTheme.typography.labelMedium)
                                },
                                onClick = {},
                                enabled = false
                            )
                            OrdenTareas.entries.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.etiqueta) },
                                    onClick = {
                                        viewModel.cambiarOrden(opcion)
                                        menuOpcionesAbierto = false
                                    },
                                    leadingIcon = {
                                        if (opcion == orden) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Borrar completadas") },
                                onClick = {
                                    menuOpcionesAbierto = false
                                    confirmarBorrado = true
                                },
                                enabled = completadas > 0,
                                leadingIcon = {
                                    Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                tareaEnEdicion = null
                mostrarDialogo = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar tarea")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (tareas.isEmpty()) {
            val mensajeVacio = when (filtro) {
                FiltroTareas.TODAS -> "No hay tareas.\nToca + para agregar una."
                FiltroTareas.PENDIENTES -> "No tienes tareas pendientes."
                FiltroTareas.COMPLETADAS -> "Aún no hay tareas completadas."
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mensajeVacio,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tareas, key = { it.id }) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        onAlternar = { viewModel.alternarCompletada(tarea) },
                        onEditar = {
                            tareaEnEdicion = tarea
                            mostrarDialogo = true
                        },
                        onEliminar = {
                            viewModel.eliminar(tarea)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val resultado = snackbarHostState.showSnackbar(
                                    message = "Tarea eliminada",
                                    actionLabel = "Deshacer",
                                    duration = SnackbarDuration.Short
                                )
                                if (resultado == SnackbarResult.ActionPerformed) {
                                    viewModel.restaurar(tarea)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoTarea(
            tarea = tareaEnEdicion,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { titulo, descripcion ->
                val tarea = tareaEnEdicion
                if (tarea == null) {
                    viewModel.agregar(titulo, descripcion)
                } else {
                    viewModel.editar(tarea, titulo, descripcion)
                }
                mostrarDialogo = false
            }
        )
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Borrar completadas") },
            text = {
                Text("Se eliminarán $completadas tareas completadas. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarCompletadas()
                    confirmarBorrado = false
                }) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TareaItem(
    tarea: Tarea,
    onAlternar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorBase = MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) colorBase.copy(alpha = 0.5f) else colorBase
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onAlternar() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                    color = if (tarea.completada) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (tarea.descripcion.isNotBlank()) {
                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = formatearFecha(tarea.fechaCreacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DialogoTarea(
    tarea: Tarea?,
    onDismiss: () -> Unit,
    onGuardar: (titulo: String, descripcion: String) -> Unit
) {
    var titulo by remember { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tarea?.descripcion ?: "") }
    var mostrarError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tarea == null) "Nueva tarea" else "Editar tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        mostrarError = false
                    },
                    label = { Text("Título") },
                    singleLine = true,
                    isError = mostrarError,
                    supportingText = if (mostrarError) {
                        { Text("El título no puede estar vacío") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (titulo.isBlank()) {
                    mostrarError = true
                } else {
                    onGuardar(titulo.trim(), descripcion.trim())
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

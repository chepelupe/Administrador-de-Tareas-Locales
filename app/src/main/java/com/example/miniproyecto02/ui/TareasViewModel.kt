package com.example.miniproyecto02.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproyecto02.data.Tarea
import com.example.miniproyecto02.data.TareasDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroTareas(val etiqueta: String) {
    TODAS("Todas"),
    PENDIENTES("Pendientes"),
    COMPLETADAS("Completadas")
}

enum class OrdenTareas(val etiqueta: String) {
    FECHA_RECIENTE("Fecha: más recientes"),
    FECHA_ANTIGUA("Fecha: más antiguas"),
    ESTADO("Estado: pendientes primero")
}

class TareasViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TareasDatabase.getDatabase(application).tareaDao()

    private val _filtro = MutableStateFlow(FiltroTareas.TODAS)
    val filtro: StateFlow<FiltroTareas> = _filtro.asStateFlow()

    private val _orden = MutableStateFlow(OrdenTareas.FECHA_RECIENTE)
    val orden: StateFlow<OrdenTareas> = _orden.asStateFlow()

    // Lista ya filtrada y ordenada; se actualiza sola cuando cambia la BD, el filtro o el orden
    val tareas: StateFlow<List<Tarea>> = combine(
        dao.obtenerTodas(),
        _filtro,
        _orden
    ) { lista, filtro, orden ->
        val filtradas = when (filtro) {
            FiltroTareas.TODAS -> lista
            FiltroTareas.PENDIENTES -> lista.filter { !it.completada }
            FiltroTareas.COMPLETADAS -> lista.filter { it.completada }
        }
        when (orden) {
            OrdenTareas.FECHA_RECIENTE -> filtradas.sortedByDescending { it.fechaCreacion }
            OrdenTareas.FECHA_ANTIGUA -> filtradas.sortedBy { it.fechaCreacion }
            OrdenTareas.ESTADO -> filtradas.sortedWith(
                compareBy<Tarea> { it.completada }.thenByDescending { it.fechaCreacion }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientes: StateFlow<Int> = dao.contarPendientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completadas: StateFlow<Int> = dao.contarCompletadas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun cambiarFiltro(filtro: FiltroTareas) {
        _filtro.value = filtro
    }

    fun cambiarOrden(orden: OrdenTareas) {
        _orden.value = orden
    }

    fun agregar(titulo: String, descripcion: String) {
        viewModelScope.launch {
            dao.insertar(Tarea(titulo = titulo, descripcion = descripcion))
        }
    }

    fun editar(tarea: Tarea, titulo: String, descripcion: String) {
        viewModelScope.launch {
            dao.actualizar(tarea.copy(titulo = titulo, descripcion = descripcion))
        }
    }

    fun alternarCompletada(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizar(tarea.copy(completada = !tarea.completada))
        }
    }

    fun eliminar(tarea: Tarea) {
        viewModelScope.launch {
            dao.eliminar(tarea)
        }
    }

    // Se usa para el botón "Deshacer" después de eliminar
    fun restaurar(tarea: Tarea) {
        viewModelScope.launch {
            dao.insertar(tarea)
        }
    }

    fun borrarCompletadas() {
        viewModelScope.launch {
            dao.eliminarCompletadas()
        }
    }
}

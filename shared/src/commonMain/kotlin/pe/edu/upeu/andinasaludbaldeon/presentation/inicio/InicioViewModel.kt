package pe.edu.upeu.andinasaludbaldeon.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerPacienteUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerProximaCitaUseCase
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.aUi

class InicioViewModel(
    private val obtenerPaciente: ObtenerPacienteUseCase,
    private val obtenerProximaCita: ObtenerProximaCitaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<InicioUiState>(InicioUiState.Cargando)
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()
    private var carga: Job? = null

    init { cargar() }

    // Actualiza la tarjeta al volver desde el formulario o el detalle.
    fun cargar() {
        carga?.cancel()
        _uiState.value = InicioUiState.Cargando
        carga = viewModelScope.launch {
            val paciente = obtenerPaciente()
            ensureActive()
            if (paciente.isFailure) {
                mostrarError()
                return@launch
            }
            val datosPaciente = paciente.getOrThrow()
            val proxima = obtenerProximaCita(datosPaciente.id)
            ensureActive()
            _uiState.value = proxima.fold(
                onSuccess = {
                    if (it == null) InicioUiState.SinCitas(datosPaciente.nombre)
                    else InicioUiState.Contenido(datosPaciente.nombre, it.aUi())
                },
                onFailure = { InicioUiState.Error("No se pudo cargar el inicio. Inténtalo de nuevo.") }
            )
        }
    }

    fun reintentar() = cargar()

    private fun mostrarError() {
        _uiState.value = InicioUiState.Error("No se pudo cargar el inicio. Inténtalo de nuevo.")
    }
}

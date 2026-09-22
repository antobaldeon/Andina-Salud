package pe.edu.upeu.andinasaludbaldeon.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ObtenerPacienteUseCase

class CitasViewModel(
    private val obtenerPaciente: ObtenerPacienteUseCase,
    private val obtenerCitas: ObtenerCitasUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CitasUiState())
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()

    private var todasLasCitas: List<CitaUi> = emptyList()
    private var carga: Job? = null

    init { cargar() }

    // Volver a llamar al regresar de registrar o cancelar una cita.
    fun cargar() {
        carga?.cancel()
        _uiState.update { it.copy(fase = FaseCitas.Cargando) }
        carga = viewModelScope.launch {
            val paciente = obtenerPaciente()
            ensureActive()
            if (paciente.isFailure) {
                mostrarError()
                return@launch
            }
            val citas = obtenerCitas(paciente.getOrThrow().id)
            ensureActive()
            citas.fold(
                onSuccess = { resultado ->
                    todasLasCitas = resultado.map { it.aUi() }
                    _uiState.update { it.copy(citasProgramadas = todasLasCitas.count { cita -> cita.estado == FiltroEstadoCita.PROGRAMADA }, fase = filtrar(it)) }
                },
                onFailure = { mostrarError() }
            )
        }
    }

    fun reintentar() = cargar()

    fun onBusquedaChange(valor: String) {
        actualizarFiltros { it.copy(busqueda = valor) }
    }

    fun onFiltroChange(filtro: FiltroEstadoCita) {
        actualizarFiltros { it.copy(filtro = filtro) }
    }

    fun onSoloHoyChange(valor: Boolean) { actualizarFiltros { it.copy(soloHoy = valor) } }

    private fun actualizarFiltros(cambio: (CitasUiState) -> CitasUiState) {
        _uiState.update {
            val nuevo = cambio(it)
            when (it.fase) {
                is FaseCitas.Contenido, is FaseCitas.Vacio -> nuevo.copy(fase = filtrar(nuevo))
                else -> nuevo
            }
        }
    }

    private fun filtrar(estado: CitasUiState): FaseCitas {
        val consulta = estado.busqueda.normalizarBusqueda()
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.of("America/Lima")).date
        val coincidencias = todasLasCitas.filter {
            it.estado == estado.filtro &&
                (!estado.soloHoy || it.fechaValor == hoy) &&
                (consulta in it.especialidad.normalizarBusqueda() || consulta in it.medico.normalizarBusqueda())
        }
        return if (coincidencias.isNotEmpty()) FaseCitas.Contenido(coincidencias)
        else FaseCitas.Vacio(
            if (todasLasCitas.isEmpty()) "Todavía no tienes citas."
            else "No hay citas que coincidan con los filtros."
        )
    }

    private fun mostrarError() {
        _uiState.update { it.copy(fase = FaseCitas.Error("No se pudieron cargar tus citas. Inténtalo de nuevo.")) }
    }
}

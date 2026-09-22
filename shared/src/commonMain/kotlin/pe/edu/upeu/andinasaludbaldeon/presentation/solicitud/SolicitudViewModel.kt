package pe.edu.upeu.andinasaludbaldeon.presentation.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pe.edu.upeu.andinasaludbaldeon.domain.model.SolicitudCita
import pe.edu.upeu.andinasaludbaldeon.domain.model.ModalidadAtencion
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.*

class SolicitudViewModel(
    private val reloj: Reloj,
    private val paciente: ObtenerPacienteUseCase,
    private val catalogo: ObtenerCatalogoUseCase,
    private val validar: ValidarSolicitudCitaUseCase,
    private val solicitar: SolicitarCitaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val manana = reloj.ahora().toLocalDateTime(reloj.zonaHoraria).date.plus(1, DateTimeUnit.DAY)
        _uiState.update { it.copy(fecha = manana.toString(), hora = "09:00") }
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensaje = null) }
            catalogo().fold(
                onSuccess = { _uiState.update { s -> s.copy(cargando = false, especialidades = it.especialidades, sedes = it.sedes) } },
                onFailure = { _uiState.update { it.copy(cargando = false, mensaje = "No se pudo cargar el formulario. Inténtalo de nuevo.") } }
            )
        }
    }

    fun elegirEspecialidad(id: String) = _uiState.update { it.copy(especialidadId = id, errores = it.errores.copy(especialidad = null)) }
    fun elegirSede(id: String) = _uiState.update { it.copy(sedeId = id, errores = it.errores.copy(sede = null)) }
    fun cambiarFecha(v: String) = _uiState.update { it.copy(fecha = v, errores = it.errores.copy(fecha = null)) }
    fun cambiarHora(v: String) = _uiState.update { it.copy(hora = v, errores = it.errores.copy(hora = null)) }
    fun cambiarMotivo(v: String) = _uiState.update { it.copy(motivo = v.take(200), errores = it.errores.copy(motivo = null)) }
    fun cambiarModalidad(v: ModalidadAtencion) = _uiState.update { it.copy(modalidad = v) }

    fun enviar() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true, mensaje = null) }
            val pacienteResultado = paciente()
            if (pacienteResultado.isFailure) {
                _uiState.update { it.copy(enviando = false, mensaje = "No se pudo identificar al paciente.") }
                return@launch
            }
            val fecha = runCatching { LocalDate.parse(s.fecha) }.getOrNull()
            val hora = runCatching { LocalTime.parse(s.hora) }.getOrNull()
            if (fecha == null || hora == null) {
                _uiState.update { it.copy(enviando = false, errores = it.errores.copy(
                    fecha = if (fecha == null) "Usa el formato AAAA-MM-DD." else null,
                    hora = if (hora == null) "Usa el formato HH:MM." else null
                )) }
                return@launch
            }
            val request = SolicitudCita(pacienteResultado.getOrThrow().id, s.especialidadId, s.sedeId, fecha, hora, s.motivo, s.modalidad)
            val errores = validar(request, emptyList()).copy(
                general = if (s.especialidadId.isBlank()) "Selecciona una especialidad." else null
            )
            if (errores.hayErrores) {
                _uiState.update { it.copy(enviando = false, errores = errores) }
                return@launch
            }
            solicitar(request).fold(
                onSuccess = { _uiState.update { it.copy(enviando = false, completada = true, mensaje = "Cita solicitada correctamente.") } },
                onFailure = { error ->
                    val campos = (error as? SolicitudCitaInvalidaException)?.errores
                    _uiState.update { it.copy(enviando = false, errores = campos ?: it.errores, mensaje = if (campos == null) "No se pudo solicitar la cita. Inténtalo de nuevo." else null) }
                }
            )
        }
    }
}

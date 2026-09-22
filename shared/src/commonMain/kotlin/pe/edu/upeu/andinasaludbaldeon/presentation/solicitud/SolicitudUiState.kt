package pe.edu.upeu.andinasaludbaldeon.presentation.solicitud

import pe.edu.upeu.andinasaludbaldeon.domain.model.Especialidad
import pe.edu.upeu.andinasaludbaldeon.domain.model.Sede
import pe.edu.upeu.andinasaludbaldeon.domain.model.ModalidadAtencion
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.ErroresSolicitudCita

data class SolicitudUiState(
    val cargando: Boolean = true,
    val enviando: Boolean = false,
    val especialidades: List<Especialidad> = emptyList(),
    val sedes: List<Sede> = emptyList(),
    val especialidadId: String = "",
    val sedeId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    val modalidad: ModalidadAtencion = ModalidadAtencion.PRESENCIAL,
    val errores: ErroresSolicitudCita = ErroresSolicitudCita(),
    val mensaje: String? = null,
    val completada: Boolean = false
)

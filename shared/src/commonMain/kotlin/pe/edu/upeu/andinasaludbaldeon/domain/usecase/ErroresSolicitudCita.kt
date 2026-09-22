package pe.edu.upeu.andinasaludbaldeon.domain.usecase

data class ErroresSolicitudCita(
    val paciente: String? = null,
    val especialidad: String? = null,
    val sede: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val motivo: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(paciente, especialidad, sede, fecha, hora, motivo, general).any { it != null }
}

class SolicitudCitaInvalidaException(val errores: ErroresSolicitudCita) :
    IllegalArgumentException("La solicitud contiene errores.")

class CitaNoEncontradaException : NoSuchElementException("No se encontro la cita del paciente.")

class CancelacionNoPermitidaException :
    IllegalStateException("Solo se puede cancelar una cita Programada a mas de 24 horas.")

class ReprogramacionNoPermitidaException :
    IllegalStateException("Solo se puede reprogramar una cita Programada.")

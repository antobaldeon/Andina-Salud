package pe.edu.upeu.andinasaludbaldeon.presentation.citas

import pe.edu.upeu.andinasaludbaldeon.domain.model.Cita
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import kotlinx.datetime.number

enum class FiltroEstadoCita(val etiqueta: String) {
    PROGRAMADA("Programada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada")
}

data class CitaUi(
    val id: Long,
    val especialidad: String,
    val medico: String,
    val sede: String,
    val fecha: String,
    val hora: String,
    val estado: FiltroEstadoCita
)

internal fun Cita.aUi() = CitaUi(
    id = id,
    especialidad = especialidad.nombre,
    medico = medico.nombre,
    sede = sede.nombre,
    fecha = "${fecha.day.toString().padStart(2, '0')}/${fecha.month.number.toString().padStart(2, '0')}/${fecha.year}",
    hora = "${hora.hour.toString().padStart(2, '0')}:${hora.minute.toString().padStart(2, '0')}",
    estado = when (estado) {
        is EstadoCita.Programada -> FiltroEstadoCita.PROGRAMADA
        is EstadoCita.Atendida -> FiltroEstadoCita.ATENDIDA
        is EstadoCita.Cancelada -> FiltroEstadoCita.CANCELADA
    }
)

// Incluye vocales acentuadas precompuestas y marcas combinadas, sin APIs de Java.
internal fun String.normalizarBusqueda(): String = buildString {
    for (caracter in this@normalizarBusqueda.trim().lowercase()) {
        when (caracter) {
            'á', 'à', 'ä', 'â', 'ã' -> append('a')
            'é', 'è', 'ë', 'ê' -> append('e')
            'í', 'ì', 'ï', 'î' -> append('i')
            'ó', 'ò', 'ö', 'ô', 'õ' -> append('o')
            'ú', 'ù', 'ü', 'û' -> append('u')
            'ñ' -> append('n')
            in '\u0300'..'\u036f' -> Unit
            else -> append(caracter)
        }
    }
}

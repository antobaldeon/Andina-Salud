package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.*

class SolicitarCitaUseCase(
    private val citas: CitaRepository,
    private val pacientes: PacienteRepository,
    private val catalogo: CatalogoRepository,
    private val validar: ValidarSolicitudCitaUseCase
) {
    suspend operator fun invoke(solicitud: SolicitudCita): Result<Cita> = resultadoDe {
        citas.conAccesoExclusivo {
            val paciente = pacientes.obtenerPaciente()
            val especialidad = catalogo.obtenerEspecialidades().find { it.id == solicitud.especialidadId }
            val sede = catalogo.obtenerSedes().find { it.id == solicitud.sedeId }
            val medico = catalogo.obtenerMedicos().firstOrNull {
                it.especialidad.id == solicitud.especialidadId && it.sedes.any { sede -> sede.id == solicitud.sedeId }
            }
            val errores = validar(solicitud, citas.obtenerPorPaciente(solicitud.pacienteId))
            val completos = errores.copy(
                paciente = errores.paciente ?: if (paciente.id != solicitud.pacienteId) "El paciente no existe." else null,
                especialidad = errores.especialidad ?: if (especialidad == null) "La especialidad no existe." else null,
                sede = errores.sede ?: when {
                    sede == null -> "La sede no existe."
                    especialidad != null && medico == null -> "No hay medicos de esa especialidad en esta sede."
                    else -> null
                }
            )
            if (completos.hayErrores) throw SolicitudCitaInvalidaException(completos)
            citas.registrar(
                Cita(
                    id = 0L,
                    paciente = paciente,
                    medico = checkNotNull(medico),
                    sede = checkNotNull(sede),
                    fecha = solicitud.fecha,
                    hora = solicitud.hora,
                    motivo = solicitud.motivo.trim(),
                    estado = EstadoCita.Programada(recordatorioActivo = false)
                )
            )
        }
    }
}

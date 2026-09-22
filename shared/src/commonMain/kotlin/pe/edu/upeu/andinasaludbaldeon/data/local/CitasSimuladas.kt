package pe.edu.upeu.andinasaludbaldeon.data.local

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj

// Se crea una sola vez por sesion; las fechas se calculan al iniciar.
class CitasSimuladas(reloj: Reloj) {
    val paciente = Paciente(
        id = "P-0417",
        nombre = "Lucía Quispe Mamani",
        documento = "70154823",
        correo = "lucia.quispe@correo.pe",
        telefono = "987654321"
    )
    val sedes = listOf(
        Sede("S1", "Ñaña"), Sede("S2", "Chosica"),
        Sede("S3", "Chaclacayo"), Sede("S4", "Santa Anita")
    )
    val especialidades = listOf(
        Especialidad("E1", "Medicina General"),
        Especialidad("E2", "Odontología"),
        Especialidad("E3", "Pediatría"),
        Especialidad("E4", "Nutrición"),
        Especialidad("E5", "Psicología")
    )
    val medicos = listOf(
        Medico("M1", "Dr. Iván Rojas", especialidades[0], listOf(sedes[0], sedes[1])),
        Medico("M2", "Dra. Elena Torres", especialidades[0], listOf(sedes[2], sedes[3])),
        Medico("M3", "Dra. Rosa Flores", especialidades[1], listOf(sedes[0], sedes[1])),
        Medico("M4", "Dr. Pedro Salas", especialidades[1], listOf(sedes[2], sedes[3])),
        Medico("M5", "Dra. Carla Núñez", especialidades[2], listOf(sedes[0], sedes[2])),
        Medico("M6", "Dr. Luis Vega", especialidades[2], listOf(sedes[1], sedes[3])),
        Medico("M7", "Lic. Ana Bermúdez", especialidades[3], listOf(sedes[0], sedes[3])),
        Medico("M8", "Lic. Jorge Castro", especialidades[3], listOf(sedes[1], sedes[2])),
        Medico("M9", "Ps. Luis Tapia", especialidades[4], listOf(sedes[0], sedes[1])),
        Medico("M10", "Ps. María Ramos", especialidades[4], listOf(sedes[2], sedes[3]))
    )

    private val hoy = reloj.ahora().toLocalDateTime(reloj.zonaHoraria).date

    val citas = listOf(
        crearCita(1, 0, 0, 2, "09:00", EstadoCita.Programada(true)),
        crearCita(2, 2, 1, 4, "16:30", EstadoCita.Programada(false)),
        crearCita(3, 6, 3, 7, "11:15", EstadoCita.Programada(true)),
        crearCita(4, 4, 2, -20, "08:45", EstadoCita.Atendida("Control en tres meses")),
        crearCita(5, 8, 0, -10, "15:00", EstadoCita.Atendida("Continuar sesiones quincenales")),
        crearCita(6, 0, 1, -5, "10:30", EstadoCita.Cancelada("Viaje del paciente", true))
    )

    private fun crearCita(
        id: Long, medico: Int, sede: Int, dias: Int, hora: String, estado: EstadoCita
    ) = Cita(
        id = id, paciente = paciente, medico = medicos[medico], sede = sedes[sede],
        fecha = hoy.plus(dias, DateTimeUnit.DAY), hora = LocalTime.parse(hora),
        motivo = "Consulta de ${medicos[medico].especialidad.nombre.lowercase()}",
        estado = estado
    )
}

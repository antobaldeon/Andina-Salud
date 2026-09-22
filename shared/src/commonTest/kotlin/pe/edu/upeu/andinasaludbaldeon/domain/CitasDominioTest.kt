package pe.edu.upeu.andinasaludbaldeon.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.datetime.*
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.policy.PoliticasCita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.*
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.*
import kotlin.test.*
import kotlin.time.Instant

class CitasDominioTest {
    private val paciente = Paciente("P1", "Antonella", "70154823", "lucia@correo.pe", "999111222")
    private val sede = Sede("S1", "ÑaÑa")
    private val especialidad = Especialidad("E1", "Medicina General")
    private val medico = Medico("M1", "Ivan Rojas", especialidad, listOf(sede))
    private val reloj = object : Reloj {
        override val zonaHoraria = TimeZone.of("America/Lima")
        // 22 de septiembre, 10:00 en Lima.
        override fun ahora() = Instant.parse("2026-09-22T15:00:00Z")
    }
    private val validar = ValidarSolicitudCitaUseCase(reloj)
    private val pacientes = object : PacienteRepository {
        override suspend fun obtenerPaciente() = paciente
    }
    private fun catalogo(medicos: List<Medico> = listOf(medico)) = object : CatalogoRepository {
        override suspend fun obtenerEspecialidades() = listOf(especialidad)
        override suspend fun obtenerSedes() = listOf(sede)
        override suspend fun obtenerMedicos() = medicos
    }
    private fun solicitud(
        fecha: String = "2026-09-25",
        hora: String = "09:00",
        motivo: String = "Consulta por dolor de cabeza"
    ) = SolicitudCita("P1", "E1", "S1", LocalDate.parse(fecha), LocalTime.parse(hora), motivo)

    private fun cita(
        id: Long = 1,
        fecha: String = "2026-09-25",
        hora: String = "09:00",
        estado: EstadoCita = EstadoCita.Programada(false),
        titular: Paciente = paciente
    ) = Cita(id, titular, medico, sede, LocalDate.parse(fecha), LocalTime.parse(hora), "Consulta general", estado)

    // Dobles exclusivos de pruebas: la implementacion de data sigue pendiente.
    private class RepositorioPrueba(iniciales: List<Cita> = emptyList()) : CitaRepository {
        val datos = iniciales.toMutableList()
        private val mutex = Mutex()
        override suspend fun obtenerPorPaciente(pacienteId: String) = datos.filter { it.paciente.id == pacienteId }
        override suspend fun obtenerPorId(id: Long) = datos.find { it.id == id }
        override suspend fun registrar(cita: Cita): Cita {
            yield()
            return cita.copy(id = (datos.maxOfOrNull { it.id } ?: 0L) + 1).also { datos.add(it) }
        }
        override suspend fun actualizar(cita: Cita): Cita {
            val indice = datos.indexOfFirst { it.id == cita.id }
            check(indice >= 0)
            datos[indice] = cita
            return cita
        }
        override suspend fun <T> conAccesoExclusivo(operacion: suspend () -> T): T =
            mutex.withLock { operacion() }
    }

    private fun registrar(repo: CitaRepository, medicos: List<Medico> = listOf(medico)) =
        SolicitarCitaUseCase(repo, pacientes, catalogo(medicos), validar)

    @Test fun rechazaFechaPasada() {
        assertNotNull(validar(solicitud(fecha = "2026-09-21"), emptyList()).fecha)
    }

    @Test fun rechazaHoraPasadaDelDiaActual() {
        val errores = validar(solicitud(fecha = "2026-09-22", hora = "09:59"), emptyList())
        assertNull(errores.fecha)
        assertNotNull(errores.hora)
    }

    @Test fun aceptaMomentoActualYFuturo() {
        assertFalse(validar(solicitud(fecha = "2026-09-22", hora = "10:00"), emptyList()).hayErrores)
        assertFalse(validar(solicitud(), emptyList()).hayErrores)
    }

    @Test fun motivoAceptaLimitesYRechazaFueraDeRango() {
        for (longitud in listOf(10, 200)) {
            assertNull(validar(solicitud(motivo = "a".repeat(longitud)), emptyList()).motivo)
        }
        for (longitud in listOf(0, 9, 201)) {
            assertNotNull(validar(solicitud(motivo = "a".repeat(longitud)), emptyList()).motivo)
        }
        assertNotNull(validar(solicitud(motivo = "   corto   "), emptyList()).motivo)
    }

    @Test fun exigeIdentificadores() {
        val errores = validar(solicitud().copy(pacienteId = "", especialidadId = "", sedeId = ""), emptyList())
        assertNotNull(errores.paciente)
        assertNotNull(errores.especialidad)
        assertNotNull(errores.sede)
    }

    @Test fun limiteSoloCuentaProgramadasDelPaciente() {
        val dos = listOf(cita(1), cita(2))
        val otras = listOf(
            cita(3, estado = EstadoCita.Atendida("Control")),
            cita(4, estado = EstadoCita.Cancelada("Viaje", true)),
            cita(5, titular = paciente.copy(id = "P2"))
        )
        assertTrue(PoliticasCita.puedeSolicitar(dos + otras, "P1"))
        assertFalse(PoliticasCita.puedeSolicitar(dos + cita(6), "P1"))
        assertNotNull(validar(solicitud(hora = "12:00"), dos + cita(6)).general)
    }

    @Test fun rechazaHorarioDuplicadoPeroPermiteOtraHora() {
        assertNotNull(validar(solicitud(), listOf(cita())).hora)
        assertNull(validar(solicitud(hora = "10:00"), listOf(cita())).hora)
        assertNull(validar(solicitud(), listOf(cita(fecha = "2026-09-26"))).hora)
    }

    @Test fun duplicadoIgnoraOtrosEstadosYPacientes() {
        val otras = listOf(
            cita(1, estado = EstadoCita.Atendida("Control")),
            cita(2, estado = EstadoCita.Cancelada("Viaje", true)),
            cita(3, titular = paciente.copy(id = "P2"))
        )
        assertFalse(validar(solicitud(), otras).hayErrores)
    }

    @Test fun cancelacionExigeMasDe24HorasExactas() {
        for (hora in listOf("09:59", "10:00")) {
            assertFalse(PoliticasCita.puedeCancelar(cita(fecha = "2026-09-23", hora = hora), reloj.ahora(), reloj.zonaHoraria))
        }
        assertTrue(PoliticasCita.puedeCancelar(cita(fecha = "2026-09-23", hora = "10:00:01"), reloj.ahora(), reloj.zonaHoraria))
        assertFalse(PoliticasCita.puedeCancelar(cita(fecha = "2026-09-21"), reloj.ahora(), reloj.zonaHoraria))
    }

    @Test fun noCancelaAtendidasNiCanceladas() {
        for (estado in listOf(EstadoCita.Atendida("Control"), EstadoCita.Cancelada("Viaje", true))) {
            assertFalse(PoliticasCita.puedeCancelar(cita(estado = estado), reloj.ahora(), reloj.zonaHoraria))
        }
    }

    @Test fun registraProgramadaConMedicoCompatibleYMotivoLimpio() = runTest {
        val repo = RepositorioPrueba()
        val otro = medico.copy(id = "M2", especialidad = Especialidad("E2", "Pediatria"))
        val registrada = registrar(repo, listOf(otro, medico))(solicitud(motivo = "  Consulta general  ")).getOrThrow()
        assertEquals(1L, registrada.id)
        assertEquals(medico, registrada.medico)
        assertEquals("Consulta general", registrada.motivo)
        assertIs<EstadoCita.Programada>(registrada.estado)
        assertEquals(listOf(registrada), repo.datos)
    }

    @Test fun solicitudesInvalidasNoModificanRepositorio() = runTest {
        val repo = RepositorioPrueba()
        val casos = listOf(
            solicitud(fecha = "2026-09-21"),
            solicitud().copy(pacienteId = "P2"),
            solicitud().copy(especialidadId = "inexistente"),
            solicitud().copy(sedeId = "inexistente"),
            solicitud(motivo = "corto")
        )
        for (entrada in casos) {
            assertIs<SolicitudCitaInvalidaException>(registrar(repo)(entrada).exceptionOrNull())
        }
        assertTrue(repo.datos.isEmpty())
    }

    @Test fun rechazaSedeSinMedicoCompatible() = runTest {
        val repo = RepositorioPrueba()
        val error = registrar(repo, listOf(medico.copy(sedes = emptyList())))(solicitud()).exceptionOrNull()
        assertNotNull(assertIs<SolicitudCitaInvalidaException>(error).errores.sede)
        assertTrue(repo.datos.isEmpty())
    }

    @Test fun dosSolicitudesConcurrentesNoSuperanTres() = runTest {
        val repo = RepositorioPrueba(listOf(cita(1, hora = "07:00"), cita(2, hora = "08:00")))
        val useCase = registrar(repo)
        val resultados = listOf(
            async { useCase(solicitud(hora = "11:00")) },
            async { useCase(solicitud(hora = "12:00")) }
        ).awaitAll()
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals(3, repo.datos.size)
        assertNotNull(assertIs<SolicitudCitaInvalidaException>(resultados.first { it.isFailure }.exceptionOrNull()).errores.general)
    }

    @Test fun solicitudesConcurrentesNoDuplicanHorario() = runTest {
        val repo = RepositorioPrueba()
        val useCase = registrar(repo)
        val resultados = listOf(async { useCase(solicitud()) }, async { useCase(solicitud()) }).awaitAll()
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals(1, repo.datos.size)
    }

    @Test fun cancelaYLiberaCupoParaOtraSolicitud() = runTest {
        val repo = RepositorioPrueba(listOf(cita(1), cita(2, hora = "11:00"), cita(3, hora = "12:00")))
        val cancelada = CancelarCitaUseCase(repo, reloj)(1, "P1", "Viaje").getOrThrow()
        assertEquals(EstadoCita.Cancelada("Viaje", true), cancelada.estado)
        assertTrue(registrar(repo)(solicitud()).isSuccess)
        assertEquals(3, repo.datos.count { it.estado is EstadoCita.Programada })
    }

    @Test fun cancelacionInvalidaNoCambiaDatos() = runTest {
        val original = cita(fecha = "2026-09-23", hora = "10:00")
        val repo = RepositorioPrueba(listOf(original))
        val cancelar = CancelarCitaUseCase(repo, reloj)
        assertIs<CancelacionNoPermitidaException>(cancelar(1, "P1").exceptionOrNull())
        assertIs<CitaNoEncontradaException>(cancelar(1, "P2").exceptionOrNull())
        assertIs<CitaNoEncontradaException>(cancelar(999, "P1").exceptionOrNull())
        assertEquals(listOf(original), repo.datos)
    }

    @Test fun listaOrdenaPorFechaYHoraYSoloPacienteActual() = runTest {
        val repo = RepositorioPrueba(listOf(
            cita(1, fecha = "2026-09-26"), cita(2, hora = "11:00"), cita(3, hora = "08:00"),
            cita(4, titular = paciente.copy(id = "P2"))
        ))
        assertEquals(listOf(3L, 2L, 1L), ObtenerCitasUseCase(repo)("P1").getOrThrow().map { it.id })
        assertEquals(emptyList(), ObtenerCitasUseCase(repo)("sin-citas").getOrThrow())
    }

    @Test fun detalleVerificaExistenciaYPaciente() = runTest {
        val repo = RepositorioPrueba(listOf(cita()))
        val obtener = ObtenerDetalleCitaUseCase(repo)
        assertEquals(cita(), obtener(1, "P1").getOrThrow())
        assertIs<CitaNoEncontradaException>(obtener(1, "P2").exceptionOrNull())
        assertIs<CitaNoEncontradaException>(obtener(99, "P1").exceptionOrNull())
    }

    @Test fun proximaSoloIncluyeProgramadasNoPasadas() = runTest {
        val repo = RepositorioPrueba(listOf(
            cita(1, fecha = "2026-09-21"),
            cita(2, fecha = "2026-09-23", estado = EstadoCita.Atendida("Control")),
            cita(3, fecha = "2026-09-23", estado = EstadoCita.Cancelada("Viaje", true)),
            cita(4, fecha = "2026-09-26"),
            cita(5, fecha = "2026-09-25")
        ))
        assertEquals(5L, ObtenerProximaCitaUseCase(repo, reloj)("P1").getOrThrow()?.id)
        assertNull(ObtenerProximaCitaUseCase(repo, reloj)("sin-citas").getOrThrow())
    }

    @Test fun obtienePacienteYCatalogo() = runTest {
        assertEquals(paciente, ObtenerPacienteUseCase(pacientes)().getOrThrow())
        val resultado = ObtenerCatalogoUseCase(catalogo())().getOrThrow()
        assertEquals(listOf(sede), resultado.sedes)
        assertEquals(listOf(especialidad), resultado.especialidades)
        assertEquals(listOf(medico), resultado.medicos)
    }

    @Test fun devuelveFalloDeRepositorio() = runTest {
        val error = IllegalStateException("Error simulado")
        val repo = object : PacienteRepository {
            override suspend fun obtenerPaciente(): Paciente = throw error
        }
        assertSame(error, ObtenerPacienteUseCase(repo)().exceptionOrNull())
    }

    @Test fun noConvierteCancelacionDeCorrutinaEnErrorDePantalla() = runTest {
        val repo = object : PacienteRepository {
            override suspend fun obtenerPaciente(): Paciente = throw CancellationException("Pantalla cerrada")
        }
        assertFailsWith<CancellationException> { ObtenerPacienteUseCase(repo)() }
    }
}

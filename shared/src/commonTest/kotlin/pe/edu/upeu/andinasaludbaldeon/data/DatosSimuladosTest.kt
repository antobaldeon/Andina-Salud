package pe.edu.upeu.andinasaludbaldeon.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import pe.edu.upeu.andinasaludbaldeon.data.local.*
import pe.edu.upeu.andinasaludbaldeon.data.repository.*
import pe.edu.upeu.andinasaludbaldeon.data.time.RelojSistema
import pe.edu.upeu.andinasaludbaldeon.di.dataModule
import pe.edu.upeu.andinasaludbaldeon.di.domainModule
import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.*
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.*
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DatosSimuladosTest {
    private fun reloj(fecha: String = "2026-09-22T23:00:00Z") = object : Reloj {
        override val zonaHoraria = TimeZone.of("America/Lima")
        override fun ahora() = Instant.parse(fecha)
    }
    private val reloj = reloj()
    private val datos = CitasSimuladas(reloj)
    private val pacienteId = datos.paciente.id

    @Test fun semillaCumpleCantidadesYRelaciones() {
        assertTrue(datos.paciente.telefono.isNotBlank())
        assertTrue(datos.paciente.documento.isNotBlank())
        assertTrue(datos.paciente.correo.isNotBlank())
        assertEquals(setOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita"), datos.sedes.map { it.nombre }.toSet())
        assertEquals(5, datos.especialidades.size)
        assertEquals(10, datos.medicos.size)
        datos.especialidades.forEach { especialidad ->
            assertTrue(datos.medicos.count { it.especialidad == especialidad } >= 2)
        }
        datos.medicos.forEach {
            assertTrue(it.sedes.isNotEmpty())
            assertTrue(it.sedes.all { sede -> sede in datos.sedes })
        }
        assertEquals(6, datos.citas.size)
        assertEquals(3, datos.citas.count { it.estado is EstadoCita.Programada })
        assertEquals(2, datos.citas.count { it.estado is EstadoCita.Atendida })
        assertEquals(1, datos.citas.count { it.estado is EstadoCita.Cancelada })
        datos.citas.forEach {
            assertEquals(datos.paciente, it.paciente)
            assertTrue(it.sede in it.medico.sedes)
            assertTrue(it.medico in datos.medicos)
        }
    }

    @Test fun fechasSiguenSiendoFuturasEnOtraSesion() {
        for (instante in listOf("2026-09-22T23:59:00Z", "2030-12-31T23:59:00Z")) {
            val relojSesion = reloj(instante)
            val semilla = CitasSimuladas(relojSesion)
            for (cita in semilla.citas) {
                val instanteCita = LocalDateTime(cita.fecha, cita.hora).toInstant(relojSesion.zonaHoraria)
                if (cita.estado is EstadoCita.Programada) {
                    assertTrue(instanteCita > relojSesion.ahora())
                } else {
                    assertTrue(instanteCita < relojSesion.ahora())
                }
            }
        }
    }

    @Test fun cargaDura800MilisegundosSinBloquear() = runTest {
        val repo = PacienteRepositoryFake(datos, SimuladorCarga())
        val resultado = async { repo.obtenerPaciente() }
        runCurrent()
        advanceTimeBy(799)
        assertFalse(resultado.isCompleted)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(datos.paciente, resultado.await())
        assertEquals(800L, currentTime)
    }

    @Test fun errorSeConsumeUnaVezYPermiteReintentar() = runTest {
        val carga = SimuladorCarga()
        val obtener = ObtenerPacienteUseCase(PacienteRepositoryFake(datos, carga))
        carga.fallarProximaOperacion("Error de prueba")
        assertIs<ErrorSimuladoException>(obtener().exceptionOrNull())
        assertEquals(datos.paciente, obtener().getOrThrow())
        assertEquals(1600L, currentTime)
    }

    @Test fun catalogosYConsultasDevuelvenDatosCorrectos() = runTest {
        val carga = SimuladorCarga()
        val catalogo = CatalogoRepositoryFake(datos, carga)
        assertEquals(datos.sedes, catalogo.obtenerSedes())
        assertEquals(datos.especialidades, catalogo.obtenerEspecialidades())
        assertEquals(datos.medicos, catalogo.obtenerMedicos())
        val repo = CitaRepositoryFake(datos.citas, carga)
        assertEquals(datos.citas, repo.obtenerPorPaciente(pacienteId))
        assertEquals(datos.citas.first(), repo.obtenerPorId(1))
        assertNull(repo.obtenerPorId(999))
        assertTrue(repo.obtenerPorPaciente("otro").isEmpty())
    }

    @Test fun admiteEscenarioVacio() = runTest {
        val repo = CitaRepositoryFake(emptyList(), SimuladorCarga())
        assertEquals(emptyList(), ObtenerCitasUseCase(repo)(pacienteId).getOrThrow())
        assertNull(ObtenerProximaCitaUseCase(repo, reloj)(pacienteId).getOrThrow())
    }

    @Test fun asignaIdsUnicosYNoModificaSemilla() = runTest {
        val repo = CitaRepositoryFake(datos.citas, SimuladorCarga())
        val copiaInicial = repo.obtenerPorPaciente(pacienteId)
        val nuevas = (1..10).map {
            async { repo.registrar(datos.citas.first().copy(id = 1000)) }
        }.awaitAll()
        assertEquals((7L..16L).toSet(), nuevas.map { it.id }.toSet())
        assertEquals(16, repo.obtenerPorPaciente(pacienteId).size)
        assertEquals(6, copiaInicial.size)
        assertEquals(6, datos.citas.size)
        assertEquals(6, CitaRepositoryFake(datos.citas, SimuladorCarga()).obtenerPorPaciente(pacienteId).size)
    }

    @Test fun fallaAntesDeEscribirYNoConsumeId() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, carga)
        carga.fallarProximaOperacion()
        assertFailsWith<ErrorSimuladoException> { repo.registrar(datos.citas.first()) }
        assertEquals(datos.citas, repo.obtenerPorPaciente(pacienteId))
        assertEquals(7L, repo.registrar(datos.citas.first()).id)
    }

    @Test fun actualizaSoloCitasExistentesYErrorNoLasCambia() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, carga)
        val cancelada = datos.citas.first().copy(estado = EstadoCita.Cancelada("Viaje", true))
        carga.fallarProximaOperacion()
        assertFailsWith<ErrorSimuladoException> { repo.actualizar(cancelada) }
        assertEquals(datos.citas.first(), repo.obtenerPorId(1))
        assertFailsWith<IllegalStateException> { repo.actualizar(cancelada.copy(id = 999)) }
        assertEquals(cancelada, repo.actualizar(cancelada))
        assertEquals(cancelada, repo.obtenerPorId(1))
        assertEquals(6, repo.obtenerPorPaciente(pacienteId).size)
    }

    @Test fun cancelarCorrutinaLiberaBloqueoSinGuardar() = runTest {
        val repo = CitaRepositoryFake(datos.citas, SimuladorCarga())
        val trabajo = launch { repo.conAccesoExclusivo { repo.registrar(datos.citas.first()) } }
        runCurrent()
        advanceTimeBy(400)
        trabajo.cancelAndJoin()
        assertEquals(datos.citas, repo.obtenerPorPaciente(pacienteId))
        val nueva = repo.conAccesoExclusivo { repo.registrar(datos.citas.first()) }
        assertEquals(7L, nueva.id)
    }

    @Test fun flujoCancelarSolicitarYConcurrenciaUsaRepositoriosReales() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, carga)
        val registrar = SolicitarCitaUseCase(
            repo, PacienteRepositoryFake(datos, carga), CatalogoRepositoryFake(datos, carga),
            ValidarSolicitudCitaUseCase(reloj)
        )
        val original = datos.citas.first()
        val solicitud = SolicitudCita(
            pacienteId, original.especialidad.id, original.sede.id,
            original.fecha, original.hora, "Consulta de seguimiento"
        )
        assertIs<SolicitudCitaInvalidaException>(registrar(solicitud).exceptionOrNull())
        CancelarCitaUseCase(repo, reloj)(original.id, pacienteId).getOrThrow()
        val resultados = listOf(
            async { registrar(solicitud) },
            async { registrar(solicitud.copy(hora = LocalTime(15, 0))) }
        ).awaitAll()
        assertEquals(1, resultados.count { it.isSuccess })
        val citas = repo.obtenerPorPaciente(pacienteId)
        assertEquals(3, citas.count { it.estado is EstadoCita.Programada })
        val nueva = resultados.first { it.isSuccess }.getOrThrow()
        assertEquals(nueva, ObtenerDetalleCitaUseCase(repo)(nueva.id, pacienteId).getOrThrow())
        assertNotNull(ObtenerProximaCitaUseCase(repo, reloj)(pacienteId).getOrThrow())
    }

    @Test fun koinResuelveCasosDeUsoYComparteRepositorio() = runTest {
        val app = koinApplication {
            modules(dataModule, domainModule, module { single<Reloj> { reloj } })
        }
        try {
            val koin = app.koin
            val repo = koin.get<CitaRepository>()
            assertSame(repo, koin.get<CitaRepository>())
            assertIs<PacienteRepositoryFake>(koin.get<PacienteRepository>())
            assertIs<CatalogoRepositoryFake>(koin.get<CatalogoRepository>())
            assertEquals(800L, koin.get<SimuladorCarga>().retardoMillis)
            koin.get<ValidarSolicitudCitaUseCase>()
            koin.get<SolicitarCitaUseCase>()
            assertEquals(datos.paciente, koin.get<ObtenerPacienteUseCase>()().getOrThrow())
            assertEquals(10, koin.get<ObtenerCatalogoUseCase>()().getOrThrow().medicos.size)
            assertEquals(6, koin.get<ObtenerCitasUseCase>()(pacienteId).getOrThrow().size)
            assertNotNull(koin.get<ObtenerProximaCitaUseCase>()(pacienteId).getOrThrow())
            koin.get<CancelarCitaUseCase>()(1, pacienteId).getOrThrow()
            val detalle = koin.get<ObtenerDetalleCitaUseCase>()(1, pacienteId).getOrThrow()
            assertIs<EstadoCita.Cancelada>(detalle.estado)
            assertEquals(detalle, repo.obtenerPorId(1))
        } finally {
            app.close()
        }
    }

    @Test fun relojSistemaUsaHoraActualYZonaDeLima() {
        val antes = Clock.System.now()
        val sistema = RelojSistema()
        val ahora = sistema.ahora()
        val despues = Clock.System.now()
        assertTrue(ahora >= antes && ahora <= despues)
        assertEquals(TimeZone.of("America/Lima"), sistema.zonaHoraria)
    }
}

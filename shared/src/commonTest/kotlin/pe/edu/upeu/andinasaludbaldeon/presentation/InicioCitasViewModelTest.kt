package pe.edu.upeu.andinasaludbaldeon.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlinx.datetime.TimeZone
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import pe.edu.upeu.andinasaludbaldeon.data.local.*
import pe.edu.upeu.andinasaludbaldeon.data.repository.*
import pe.edu.upeu.andinasaludbaldeon.di.*
import pe.edu.upeu.andinasaludbaldeon.domain.model.EstadoCita
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CitaRepository
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.*
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.*
import pe.edu.upeu.andinasaludbaldeon.presentation.inicio.*
import kotlin.test.*
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InicioCitasViewModelTest {
    private val reloj = object : Reloj {
        override val zonaHoraria = TimeZone.of("America/Lima")
        override fun ahora() = Instant.parse("2026-09-22T15:00:00Z")
    }
    private val datos = CitasSimuladas(reloj)
    private lateinit var store: ViewModelStore
    private var indice = 0

    @BeforeTest fun preparar() {
        Dispatchers.setMain(StandardTestDispatcher())
        store = ViewModelStore()
    }

    @AfterTest fun limpiar() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun <T : ViewModel> conservar(vm: T): T {
        store.put("vm-${indice++}", vm)
        return vm
    }

    private fun inicio(repo: CitaRepository, carga: SimuladorCarga) = conservar(
        InicioViewModel(
            ObtenerPacienteUseCase(PacienteRepositoryFake(datos, carga)),
            ObtenerProximaCitaUseCase(repo, reloj)
        )
    )

    private fun citas(repo: CitaRepository, carga: SimuladorCarga) = conservar(
        CitasViewModel(ObtenerPacienteUseCase(PacienteRepositoryFake(datos, carga)), ObtenerCitasUseCase(repo))
    )

    @Test fun inicioCargaPacienteYProximaCita() = runTest {
        val carga = SimuladorCarga()
        val vm = inicio(CitaRepositoryFake(datos.citas, carga), carga)
        assertIs<InicioUiState.Cargando>(vm.uiState.value)
        advanceTimeBy(799)
        assertIs<InicioUiState.Cargando>(vm.uiState.value)
        advanceUntilIdle()
        val estado = assertIs<InicioUiState.Contenido>(vm.uiState.value)
        assertEquals(datos.paciente.nombre, estado.nombrePaciente)
        assertEquals(1L, estado.proximaCita.id)
        assertEquals("24/09/2026", estado.proximaCita.fecha)
        assertEquals("09:00", estado.proximaCita.hora)
        assertEquals(1600L, currentTime) // Dos consultas consecutivas de 800 ms.
    }

    @Test fun inicioSinProgramadasMantieneNombreDelPaciente() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas.filter { it.estado !is EstadoCita.Programada }, carga)
        val vm = inicio(repo, carga)
        advanceUntilIdle()
        assertEquals(InicioUiState.SinCitas(datos.paciente.nombre), vm.uiState.value)
    }

    @Test fun inicioFallaYReintenta() = runTest {
        val carga = SimuladorCarga()
        carga.fallarProximaOperacion()
        val vm = inicio(CitaRepositoryFake(datos.citas, carga), carga)
        advanceUntilIdle()
        assertIs<InicioUiState.Error>(vm.uiState.value)
        vm.reintentar()
        assertIs<InicioUiState.Cargando>(vm.uiState.value)
        advanceUntilIdle()
        assertIs<InicioUiState.Contenido>(vm.uiState.value)
    }

    @Test fun inicioRefrescaProximaCitaTrasCancelar() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, carga)
        val vm = inicio(repo, carga)
        advanceUntilIdle()
        CancelarCitaUseCase(repo, reloj)(1, datos.paciente.id).getOrThrow()
        vm.cargar()
        advanceUntilIdle()
        assertEquals(2L, assertIs<InicioUiState.Contenido>(vm.uiState.value).proximaCita.id)
    }

    @Test fun citasCargaYFiltraPorEstadoEnOrden() = runTest {
        val carga = SimuladorCarga()
        val vm = citas(CitaRepositoryFake(datos.citas.reversed(), carga), carga)
        assertIs<FaseCitas.Cargando>(vm.uiState.value.fase)
        advanceUntilIdle()
        assertEquals(listOf(1L, 2L, 3L), assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.map { it.id })
        vm.onFiltroChange(FiltroEstadoCita.ATENDIDA)
        assertEquals(listOf(4L, 5L), assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.map { it.id })
        vm.onFiltroChange(FiltroEstadoCita.CANCELADA)
        assertEquals(listOf(6L), assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.map { it.id })
        assertEquals(1600L, currentTime) // Los filtros no consultan el repositorio.
    }

    @Test fun busquedaIgnoraMayusculasYTildesYCombinaEstado() = runTest {
        val carga = SimuladorCarga()
        val vm = citas(CitaRepositoryFake(datos.citas, carga), carga)
        advanceUntilIdle()
        for (consulta in listOf("IVAN", "  Iván  ", "Iva\u0301n")) {
            vm.onBusquedaChange(consulta)
            assertEquals(listOf(1L), assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.map { it.id })
        }
        vm.onBusquedaChange("ODONTOLOGIA")
        assertEquals(2L, assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.single().id)
        vm.onFiltroChange(FiltroEstadoCita.ATENDIDA)
        assertIs<FaseCitas.Vacio>(vm.uiState.value.fase)
        vm.onBusquedaChange("NUNEZ")
        assertEquals(4L, assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.single().id)
        vm.onBusquedaChange("")
        assertEquals(2, assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.size)
    }

    @Test fun listaVaciaYBusquedaSinResultados() = runTest {
        val carga = SimuladorCarga()
        val vacio = citas(CitaRepositoryFake(emptyList(), carga), carga)
        val lleno = citas(CitaRepositoryFake(datos.citas, carga), carga)
        advanceUntilIdle()
        assertEquals("Todavía no tienes citas.", assertIs<FaseCitas.Vacio>(vacio.uiState.value.fase).mensaje)
        lleno.onBusquedaChange("inexistente")
        assertEquals("No hay citas que coincidan con los filtros.", assertIs<FaseCitas.Vacio>(lleno.uiState.value.fase).mensaje)
    }

    @Test fun filtrosDuranteCargaSeAplicanAlTerminar() = runTest {
        val carga = SimuladorCarga()
        val vm = citas(CitaRepositoryFake(datos.citas, carga), carga)
        vm.onFiltroChange(FiltroEstadoCita.ATENDIDA)
        vm.onBusquedaChange("PSICOLOGIA")
        assertIs<FaseCitas.Cargando>(vm.uiState.value.fase)
        advanceUntilIdle()
        assertEquals(5L, assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.single().id)
    }

    @Test fun citasErrorNoSeOcultaAlCambiarFiltrosYReintentoLosConserva() = runTest {
        val carga = SimuladorCarga()
        val vm = citas(CitaRepositoryFake(datos.citas, carga), carga)
        advanceUntilIdle()
        vm.onFiltroChange(FiltroEstadoCita.ATENDIDA)
        carga.fallarProximaOperacion()
        vm.cargar()
        advanceUntilIdle()
        assertIs<FaseCitas.Error>(vm.uiState.value.fase)
        vm.onBusquedaChange("NUNEZ")
        assertIs<FaseCitas.Error>(vm.uiState.value.fase)
        vm.reintentar()
        advanceUntilIdle()
        assertEquals(FiltroEstadoCita.ATENDIDA, vm.uiState.value.filtro)
        assertEquals("NUNEZ", vm.uiState.value.busqueda)
        assertEquals(4L, assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.single().id)
    }

    @Test fun fallosDeCitasTrasObtenerPacienteSeMuestranEnAmbosViewModels() = runTest {
        val cargaPaciente = SimuladorCarga()
        val cargaCitas = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, cargaCitas)
        cargaCitas.fallarProximaOperacion()
        val lista = citas(repo, cargaPaciente)
        advanceUntilIdle()
        assertIs<FaseCitas.Error>(lista.uiState.value.fase)
        cargaCitas.fallarProximaOperacion()
        val home = inicio(repo, cargaPaciente)
        advanceUntilIdle()
        assertIs<InicioUiState.Error>(home.uiState.value)
    }

    @Test fun recargaCancelaPeticionAnterior() = runTest {
        val carga = SimuladorCarga()
        val vm = citas(CitaRepositoryFake(datos.citas, carga), carga)
        advanceTimeBy(1000)
        vm.cargar()
        advanceTimeBy(600)
        runCurrent()
        assertIs<FaseCitas.Cargando>(vm.uiState.value.fase)
        advanceUntilIdle()
        assertIs<FaseCitas.Contenido>(vm.uiState.value.fase)
        assertEquals(2600L, currentTime)
    }

    @Test fun borrarViewModelCancelaCargaSinMostrarError() = runTest {
        val carga = SimuladorCarga()
        val lista = citas(CitaRepositoryFake(datos.citas, carga), carga)
        val home = inicio(CitaRepositoryFake(datos.citas, carga), carga)
        runCurrent()
        store.clear()
        advanceUntilIdle()
        assertIs<FaseCitas.Cargando>(lista.uiState.value.fase)
        assertIs<InicioUiState.Cargando>(home.uiState.value)
    }

    @Test fun listaRecargaCambiosSinPerderBusqueda() = runTest {
        val carga = SimuladorCarga()
        val repo = CitaRepositoryFake(datos.citas, carga)
        val vm = citas(repo, carga)
        advanceUntilIdle()
        vm.onBusquedaChange("IVAN")
        CancelarCitaUseCase(repo, reloj)(1, datos.paciente.id).getOrThrow()
        vm.cargar()
        advanceUntilIdle()
        assertEquals("IVAN", vm.uiState.value.busqueda)
        assertIs<FaseCitas.Vacio>(vm.uiState.value.fase)
        vm.onFiltroChange(FiltroEstadoCita.CANCELADA)
        assertEquals(listOf(6L, 1L), assertIs<FaseCitas.Contenido>(vm.uiState.value.fase).citas.map { it.id })
    }

    @Test fun koinConstruyeAmbosViewModels() = runTest {
        val app = koinApplication {
            modules(dataModule, domainModule, presentationModule, module { single<Reloj> { reloj } })
        }
        try {
            val home = conservar(app.koin.get<InicioViewModel>())
            val lista = conservar(app.koin.get<CitasViewModel>())
            advanceUntilIdle()
            assertIs<InicioUiState.Contenido>(home.uiState.value)
            assertIs<FaseCitas.Contenido>(lista.uiState.value.fase)
        } finally {
            app.close()
        }
    }
}

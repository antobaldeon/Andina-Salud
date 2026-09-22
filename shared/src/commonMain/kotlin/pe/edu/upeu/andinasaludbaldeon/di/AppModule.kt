package pe.edu.upeu.andinasaludbaldeon.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import pe.edu.upeu.andinasaludbaldeon.presentation.inicio.InicioViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.citas.CitasViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.detalle.DetalleCitaViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.solicitud.SolicitudViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.perfil.PerfilViewModel
import pe.edu.upeu.andinasaludbaldeon.presentation.ajustes.AjustesViewModel
import pe.edu.upeu.andinasaludbaldeon.data.local.CitasSimuladas
import pe.edu.upeu.andinasaludbaldeon.data.local.SimuladorCarga
import pe.edu.upeu.andinasaludbaldeon.data.repository.*
import pe.edu.upeu.andinasaludbaldeon.data.time.RelojSistema
import pe.edu.upeu.andinasaludbaldeon.domain.repository.*
import pe.edu.upeu.andinasaludbaldeon.domain.time.Reloj
import pe.edu.upeu.andinasaludbaldeon.domain.usecase.*

val dataModule = module {
    single<Reloj> { RelojSistema() }
    single { CitasSimuladas(get()) }
    single { SimuladorCarga() }
    single<CitaRepository> { CitaRepositoryFake(get<CitasSimuladas>().citas, get()) }
    single<PacienteRepository> { PacienteRepositoryFake(get(), get()) }
    single<CatalogoRepository> { CatalogoRepositoryFake(get(), get()) }
}

val domainModule = module {
    factory { ValidarSolicitudCitaUseCase(get()) }
    factory { ObtenerCitasUseCase(get()) }
    factory { ObtenerDetalleCitaUseCase(get()) }
    factory { ObtenerProximaCitaUseCase(get(), get()) }
    factory { ObtenerPacienteUseCase(get()) }
    factory { ObtenerCatalogoUseCase(get()) }
    factory { SolicitarCitaUseCase(get(), get(), get(), get()) }
    factory { CancelarCitaUseCase(get(), get()) }
}

val presentationModule = module {
    viewModelOf(::InicioViewModel)
    viewModelOf(::CitasViewModel)
    viewModelOf(::DetalleCitaViewModel)
    viewModelOf(::SolicitudViewModel)
    viewModelOf(::PerfilViewModel)
    viewModelOf(::AjustesViewModel)
}

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}) = startKoin {
    config()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

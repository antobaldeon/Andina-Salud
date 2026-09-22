package pe.edu.upeu.andinasaludbaldeon.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule = module {
    // Registrar aqui los repositorios cuando tengan implementacion.
}

val domainModule = module {
    // Registrar aqui los casos de uso cuando tengan implementacion.
}

val presentationModule = module {
    // Registrar aqui los ViewModel cuando tengan implementacion.
}

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}) = startKoin {
    config()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

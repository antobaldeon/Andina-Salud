package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CatalogoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class CatalogoCitas(
    val especialidades: List<Especialidad>,
    val sedes: List<Sede>,
    val medicos: List<Medico>
)

class ObtenerCatalogoUseCase(private val repository: CatalogoRepository) {
    suspend operator fun invoke(): Result<CatalogoCitas> = resultadoDe {
        coroutineScope {
            val especialidades = async { repository.obtenerEspecialidades() }
            val sedes = async { repository.obtenerSedes() }
            val medicos = async { repository.obtenerMedicos() }
            CatalogoCitas(especialidades.await(), sedes.await(), medicos.await())
        }
    }
}

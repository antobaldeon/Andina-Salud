package pe.edu.upeu.andinasaludbaldeon.domain.usecase

import pe.edu.upeu.andinasaludbaldeon.domain.model.*
import pe.edu.upeu.andinasaludbaldeon.domain.repository.CatalogoRepository

data class CatalogoCitas(
    val especialidades: List<Especialidad>,
    val sedes: List<Sede>,
    val medicos: List<Medico>
)

class ObtenerCatalogoUseCase(private val repository: CatalogoRepository) {
    suspend operator fun invoke(): Result<CatalogoCitas> = resultadoDe {
        CatalogoCitas(repository.obtenerEspecialidades(), repository.obtenerSedes(), repository.obtenerMedicos())
    }
}

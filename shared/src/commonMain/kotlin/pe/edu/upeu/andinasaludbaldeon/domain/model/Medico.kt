package pe.edu.upeu.andinasaludbaldeon.domain.model

data class Medico(
    val id: String,
    val nombre: String,
    val especialidad: Especialidad,
    val sedes: List<Sede>
)

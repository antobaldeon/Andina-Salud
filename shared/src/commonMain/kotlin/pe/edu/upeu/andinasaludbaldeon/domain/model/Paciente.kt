package pe.edu.upeu.andinasaludbaldeon.domain.model

data class Paciente(
    val id: String,
    val nombre: String,
    val documento: String,
    val correo: String,
    val telefono: String
)

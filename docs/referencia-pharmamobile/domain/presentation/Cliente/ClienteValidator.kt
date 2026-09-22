package pe.edu.upeu.pharmamobile.domain.presentation.Cliente

data class ClienteValidationResult(
    val errorNombre: Boolean = false,
    val errorCorreo: Boolean = false
) {
    val esValido: Boolean
        get() = !errorNombre && !errorCorreo
}

object ClienteValidator {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun validar(nombre: String, correo: String): ClienteValidationResult {
        val errorNombre = nombre.isBlank()
        val errorCorreo = correo.isBlank() || !EMAIL_REGEX.matches(correo)

        return ClienteValidationResult(
            errorNombre = errorNombre,
            errorCorreo = errorCorreo
        )
    }
}
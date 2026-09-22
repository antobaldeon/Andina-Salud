package pe.edu.upeu.pharmamobile.demo

fun probarCliente(){
    val cliente = Cliente(
        id = 1L,
        nombre = "Farmacia Nueva Vida",
        correo = "ventas@central.pe",
        telefono = null
    )
    println(cliente.obtenerTelefono())
}

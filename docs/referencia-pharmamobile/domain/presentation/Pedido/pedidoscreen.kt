package pe.edu.upeu.pharmamobile.domain.presentation.Pedido

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PedidoScreen() {
    var clienteNombre by remember { mutableStateOf("") }
    var productoNombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registro de Pedido")

        OutlinedTextField(
            value = clienteNombre,
            onValueChange = { clienteNombre = it },
            label = { Text("Cliente") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = productoNombre,
            onValueChange = { productoNombre = it },
            label = { Text("Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val cant = cantidad.toIntOrNull()
                mensaje = when {
                    clienteNombre.isBlank() -> "Cliente obligatorio"
                    productoNombre.isBlank() -> "Producto obligatorio"
                    cant == null || cant <= 0 -> "Cantidad inválida"
                    else -> "Pedido registrado (${EstadoPedido.Pendiente})"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrar pedido") }

        mensaje?.let { Text(it) }
    }
}

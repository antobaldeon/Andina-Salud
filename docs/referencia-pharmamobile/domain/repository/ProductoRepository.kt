package pe.edu.upeu.pharmamobile.domain.repository

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>
}

# AndinaSalud

Aplicación multiplataforma para que un paciente consulte, solicite y cancele citas de una red de centros médicos. La Unidad 1 utiliza exclusivamente datos simulados en memoria; no conecta con API ni base de datos.

## Plataformas

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: abrir `iosApp/iosApp.xcodeproj` en Xcode y ejecutar el esquema `iosApp` en simulador o dispositivo.

El módulo `shared` contiene la interfaz Compose Multiplatform, el dominio y los repositorios simulados. Koin se inicia en Android desde `MainApplication` y en iOS desde `MainViewController`.

## Estructura

- `domain/model`: paciente, citas, catálogo y estados de cita.
- `domain/repository`: contratos que permiten sustituir los repositorios sin cambiar la UI ni los casos de uso.
- `domain/usecase` y `domain/policy`: validación de solicitudes, consulta de citas y cancelación.
- `data/local`: catálogo y citas semilla en memoria, además del retardo/error simulado.
- `data/repository`: implementaciones fake de los contratos del dominio.
- `presentation`: pantallas, ViewModels, estados de UI, componentes y navegación.
- `di`: módulos Koin compartidos y módulos de plataforma.

## Funciones del caso

Inicio con próxima cita; lista ordenada con filtros por estado y búsqueda sin distinguir mayúsculas ni tildes; detalle con indicaciones y cancelación confirmada; formulario de solicitud con validación de fecha, hora, motivo, cupo y horario duplicado; perfil y tema claro/oscuro.

Las reglas de negocio están en `domain`. El formulario puede crear citas durante la sesión, pero los datos se reinician al cerrar la aplicación.

## Arquitectura

La UI observa `StateFlow` expuestos por ViewModels. Los ViewModels llaman casos de uso, que dependen de interfaces de repositorio en `domain`. Las implementaciones actuales están en `data` y simulan asincronía con corrutinas. Para sustituirlas por otra fuente de datos se implementan esos contratos y se actualiza el registro de Koin.

## Colaboración y entrega del examen

Usar `develop` como rama de integración y una rama `feature/<funcionalidad>-<apellido>` por integrante. Integrar los cambios mediante solicitudes revisadas; mantener `main` estable. Para el bloque individual del examen, cada integrante crea su rama `sc-<letra>-<apellido>` y registra al menos tres commits propios. Etiquetar el commit evaluado como `v1.0-unidad1` y adjuntar las capturas Android/iOS, el gráfico de ramas y el resumen de contribuciones.

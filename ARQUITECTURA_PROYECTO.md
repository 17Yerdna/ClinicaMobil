# GUÍA MAESTRA DE APRENDIZAJE Y ARQUITECTURA: PHARMAMOBIL (SESIÓN 1 A SESIÓN 5)
> **Documento integral de contexto, arquitectura, código de referencia y evolución didáctica** para utilizar como base y contexto de inicio en proyectos multiplataforma con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**.

---

## 1. RESUMEN EJECUTIVO DEL PROYECTO
* **Nombre de la Aplicación:** PharmaMobil (Sistema de Gestión Farmacéutica Móvil).
* **Institución:** Universidad Peruana Unión (UPeU) – VIII Ciclo – Desarrollo de Aplicaciones Móviles.
* **Estudiante:** Andrey Mestanza | **GitHub:** `https://github.com/17Yerdna/PharmaMob`
* **Tecnologías Base:** Kotlin Multiplatform (`commonMain`, `androidMain`, `iosMain`), Jetpack/JetBrains Compose Multiplatform, Material 3, Koin Dependency Injection, Gradle Version Catalogs (`libs.versions.toml`).

---

## 2. LÍNEA DE TIEMPO DIDÁCTICA Y EVOLUCIÓN POR SESIONES

### 🔹 SESIÓN 1 Y 2: Fundamentos del Modelo de Dominio en Kotlin Puro
* **Objetivo:** Modelar la lógica de negocio farmacéutica sin dependencias de interfaces visuales ni sistemas operativos.
* **Conceptos clave:** Inmutabilidad con `data class`, control de invariantes con bloques `init { require(...) }`, manejo de nulos seguros con operadores Elvis (`?:`), modelado de estados con `sealed class`.
* **Archivos construidos:**
  * `Producto.kt`: Invariantes de nombre no vacío, `precio > 0` y `stock >= 0`. Funciones `verificarStock()`, `estadoDisponible()`, `valorInventario()` y `disminuirStock()`.
  * `Cliente.kt`: Manejo de teléfono opcional `String?` con fallback `obtenerTelefono(): String`.
  * `DetallePedido.kt`: Validación de `cantidad > 0` y cálculo de subtotal (`producto.precio * cantidad`).
  * `EstadoPedido.kt`: `sealed class` con estados finitos (`Pendiente`, `Procesando`, `Entregado`) y estado parametrizado `Rechazado(val motivo: String)`.
  * `Pedido.kt`: Agregación de cliente, lista de detalles y estado actual.

---

### 🔹 SESIÓN 3: Introducción a UI con Compose, Formularios y Pruebas Unitarias
* **Objetivo:** Crear la primera pantalla interactiva (`ProductoScreen`) con formulario reactivo y validar datos antes de persistir.
* **Conceptos clave:**
  * Estados observables de interfaz con `remember { mutableStateOf("") }`.
  * Conversión segura de tipos de texto a números mediante `toDoubleOrNull()` y `toIntOrNull()`.
  * Evaluación secuencial estricta de validaciones con `when`:
    1. Nombre obligatorio (`isNotBlank()`).
    2. Conversión numérica de precio.
    3. Rango de precio mayor a cero (`> 0.0`).
    4. Conversión entera de stock.
    5. Stock no negativo (`>= 0`).
  * Propiedad `isError` activada solo tras pulsar el botón (evita mostrar errores prematuros antes de escribir).
  * Creación de la suite de pruebas unitarias (`ProductoValidationTest.kt`) cubriendo 7 casos de prueba (valores válidos, vacíos, con espacios, no numéricos, ceros y negativos).

---

### 🔹 SESIÓN 4: Navegación, Theming Material 3, Tabs y Diseño Adaptativo
* **Objetivo:** Construir la navegación global entre módulos, soporte de Modo Claro/Oscuro, categorización del inventario y adaptación a tabletas.
* **Conceptos clave:**
  * **Destinos tipados:** `sealed class Screen` (`Inicio`, `Productos`, `Clientes`, `Pedidos`) con función pura `tituloPantalla(screen)`.
  * **Estructura visual estándar:** `Scaffold` + `TopAppBar` dinámica con botón de menú hamburguesa ☰ y botón conmutador de tema.
  * **Iconos vectoriales propios (`AppIcons.kt`):** Creación de iconos puros con `ImageVector.Builder` sin dependencias externas pesadas (`Menu`, `Home`, `ShoppingCart`, `Person`, `List`, `DarkMode`, `LightMode`, `Search`, `Warning`, `Info`).
  * **Theming Centralizado:** `Color.kt` y `Theme.kt` con paleta farmacéutica (Turquesa `#006874`, Superficies `#F8F9FA` / `#191C1D`), conmutación fluida entre *Light* y *Dark*.
  * **Clasificación por Pestañas (`PrimaryTabRow`):** Segmentación de productos en `Activos`, `Inactivos` y `Bajo Stock` (`stock <= 5`).
  * **Recursos Compartidos (`Compose Resources`):** Integración de `pharmamobil_logo.xml` en `composeResources/drawable/` consumido con `painterResource(Res.drawable.pharmamobil_logo)`.
  * **Diseño Adaptativo con Breakpoints:** `BoxWithConstraints` evaluando `maxWidth >= 600.dp` para alternar entre `ModalNavigationDrawer` (móviles) y `NavigationRail` lateral permanente (tablets y pantallas medianas).

---

### 🔹 SESIÓN 5: Reestructuración en Clean Architecture + MVVM + Koin
* **Objetivo:** Profesionalizar el código desacoplando la lógica de la UI, introduciendo Flujo Unidireccional de Datos (UDF), ViewModels y DI multiplataforma.
* **Problemas resueltos:**
  * Eliminación de los `remember` de lógica de negocio en la vista (el formulario ya no se vacía al rotar la pantalla).
  * Creación del contrato abstracto `ProductoRepository` en Dominio e implementación en memoria en Datos.
  * Creación del caso de uso `RegistrarProductoUseCase` retornando tipos funcionales `Result<Producto>`.
  * Modelado de 4 fases excluyentes en `ProductoUiState` (`Cargando`, `SinProductos`, `ConProductos`, `Error`).
  * Integración de **Koin** en `commonMain`, Android (`MainApplication`) e iOS (`iOSApp.swift`).

---

## 3. ARQUITECTURA DETALLADA DEL CÓDIGO (CLEAN ARCHITECTURE)

### Diagrama de Flujo y Comunicación entre Capas:
```
 [ Compose UI: ProductoScreen ]
        │  ▲
 eventos│  │ StateFlow<ProductoUiState>
        ▼  │
 [ ViewModel: ProductoViewModel ]
        │
 invoca │ operator fun invoke()
        ▼
 [ Caso de Uso: RegistrarProductoUseCase ]  <── (Reglas de validación de negocio)
        │
 usa    │ interfaz abstracta
        ▼
 [ Repositorio: ProductoRepository (domain) ]
        ▲
        │ implementa contrato
 [ Repositorio: ProductoRepositorioEnMemoria (data) ]  <── (Asigna IDs, delay de red)
```

---

## 4. CÓDIGO FUENTE ESENCIAL DE REFERENCIA

### A. Capa de Dominio: Modelos y Reglas de Negocio
#### `shared/src/commonMain/kotlin/.../domain/model/Producto.kt`
```kotlin
package pe.edu.upeu.pharmamobil.domain.model

data class Producto(
    val id: Long = 0L,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true
) {
    init {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(precio > 0) { "El precio debe ser mayor que 0" }
        require(stock >= 0) { "El stock no puede ser negativo" }
    }

    fun verificarStock(cantidad: Int): Boolean = stock >= cantidad
    fun estadoDisponible(): Boolean = stock > 0
    fun valorInventario(): Double = precio * stock

    fun disminuirStock(cantidad: Int): Producto {
        require(cantidad > 0) { "La cantidad debe ser mayor cero" }
        require(verificarStock(cantidad)) { "Stock insuficiente" }
        return copy(stock = stock - cantidad)
    }

    fun requiereReposicion(): Boolean = stock <= STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 5
    }
}
```

#### `shared/src/commonMain/kotlin/.../domain/model/EstadoPedido.kt`
```kotlin
package pe.edu.upeu.pharmamobil.domain.model

sealed class EstadoPedido {
    data object Pendiente : EstadoPedido()
    data object Procesando : EstadoPedido()
    data object Entregado : EstadoPedido()
    data class Rechazado(val motivo: String) : EstadoPedido()
}
```

---

### B. Capa de Dominio: Interfaz del Repositorio y Casos de Uso
#### `shared/src/commonMain/kotlin/.../domain/repository/ProductoRepository.kt`
```kotlin
package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>
}
```

#### `shared/src/commonMain/kotlin/.../domain/usecase/RegistrarProductoUseCase.kt`
```kotlin
package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    fun validar(nombre: String, precio: String, stock: String, activo: Boolean = true): Result<Producto> {
        val nombreTrim = nombre.trim()
        if (nombreTrim.isBlank()) return Result.failure(IllegalArgumentException("El nombre es obligatorio."))
        
        val precioDouble = precio.toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Ingrese un precio numérico."))
        if (precioDouble <= 0.0) return Result.failure(IllegalArgumentException("El precio debe ser mayor que cero."))

        val stockInt = stock.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Ingrese un stock entero."))
        if (stockInt < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo."))

        return Result.success(Producto(id = 0L, nombre = nombreTrim, precio = precioDouble, stock = stockInt, activo = activo))
    }

    suspend operator fun invoke(nombre: String, precio: String, stock: String, activo: Boolean = true): Result<Producto> {
        val validacion = validar(nombre, precio, stock, activo)
        if (validacion.isFailure) return validacion
        return try {
            val guardado = productoRepository.registrar(validacion.getOrThrow())
            Result.success(guardado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### C. Capa de Datos: Repositorio en Memoria
#### `shared/src/commonMain/kotlin/.../data/repository/ProductoRepositorioEnMemoria.kt`
```kotlin
package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {
    private val inventario = mutableListOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
        Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
        Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
    )
    private var nextId = 6L

    override suspend fun registrar(producto: Producto): Producto {
        delay(400) // Simula latencia de red para exponer estado Cargando
        val productoConId = producto.copy(id = nextId++)
        inventario.add(0, productoConId)
        return productoConId
    }

    override suspend fun listar(): List<Producto> {
        delay(500)
        return inventario.toList()
    }
}
```

---

### D. Capa de Presentación: Estado y ViewModel
#### `shared/src/commonMain/kotlin/.../presentation/producto/ProductoUiState.kt`
```kotlin
package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto

sealed interface FaseInventario {
    data object Cargando : FaseInventario
    data object SinProductos : FaseInventario
    data class ConProductos(val productos: List<Producto>) : FaseInventario
    data class Error(val mensaje: String) : FaseInventario
}

data class FormularioProductoState(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val errorNombre: String? = null,
    val errorPrecio: String? = null,
    val errorStock: String? = null,
    val estaGuardando: Boolean = false,
    val mensajeFeedback: String? = null
)

data class ProductoUiState(
    val fase: FaseInventario = FaseInventario.Cargando,
    val tabSeleccionada: Int = 0,
    val formulario: FormularioProductoState = FormularioProductoState(),
    val mostrarFormulario: Boolean = false,
    val totalActivos: Int = 0,
    val totalInactivos: Int = 0,
    val totalBajoStock: Int = 0
)
```

#### `shared/src/commonMain/kotlin/.../presentation/producto/ProductoViewModel.kt`
```kotlin
package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    private var inventarioCompleto: List<Producto> = emptyList()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = FaseInventario.Cargando) }
            try {
                inventarioCompleto = productoRepository.listar()
                actualizarFaseYContadores()
            } catch (e: Exception) {
                _uiState.update { it.copy(fase = FaseInventario.Error(e.message ?: "Error al cargar")) }
            }
        }
    }

    fun seleccionarTab(index: Int) {
        _uiState.update { it.copy(tabSeleccionada = index, mostrarFormulario = false) }
        actualizarFaseYContadores()
    }

    fun toggleMostrarFormulario() {
        _uiState.update { it.copy(mostrarFormulario = !it.mostrarFormulario) }
    }

    fun onNombreChanged(nuevo: String) = _uiState.update {
        it.copy(formulario = it.formulario.copy(nombre = nuevo, errorNombre = null))
    }
    fun onPrecioChanged(nuevo: String) = _uiState.update {
        it.copy(formulario = it.formulario.copy(precio = nuevo, errorPrecio = null))
    }
    fun onStockChanged(nuevo: String) = _uiState.update {
        it.copy(formulario = it.formulario.copy(stock = nuevo, errorStock = null))
    }
    fun onActivoChanged(nuevo: Boolean) = _uiState.update {
        it.copy(formulario = it.formulario.copy(activo = nuevo))
    }

    fun registrarProducto() {
        val form = _uiState.value.formulario
        viewModelScope.launch {
            _uiState.update { it.copy(formulario = it.formulario.copy(estaGuardando = true)) }
            val resultado = registrarProductoUseCase(form.nombre, form.precio, form.stock, form.activo)
            resultado.onSuccess { nuevo ->
                inventarioCompleto = listOf(nuevo) + inventarioCompleto
                _uiState.update {
                    it.copy(formulario = FormularioProductoState(), mostrarFormulario = false)
                }
                actualizarFaseYContadores()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(formulario = it.formulario.copy(estaGuardando = false, mensajeFeedback = error.message))
                }
            }
        }
    }

    private fun actualizarFaseYContadores() {
        val activos = inventarioCompleto.filter { it.activo }
        val inactivos = inventarioCompleto.filter { !it.activo }
        val bajoStock = inventarioCompleto.filter { it.requiereReposicion() }

        val filtrados = when (_uiState.value.tabSeleccionada) {
            0 -> activos
            1 -> inactivos
            2 -> bajoStock
            else -> inventarioCompleto
        }

        _uiState.update {
            it.copy(
                fase = if (filtrados.isEmpty()) FaseInventario.SinProductos else FaseInventario.ConProductos(filtrados),
                totalActivos = activos.size,
                totalInactivos = inactivos.size,
                totalBajoStock = bajoStock.size
            )
        }
    }
}
```

---

### E. Inyección de Dependencias: Configuración Koin Multiplataforma
#### `shared/src/commonMain/kotlin/.../di/AppModule.kt`
```kotlin
package pe.edu.upeu.pharmamobil.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel

val dataModule = module {
    singleOf(::ProductoRepositorioEnMemoria) bind ProductoRepository::class
}

val domainModule = module {
    factoryOf(::RegistrarProductoUseCase)
}

val viewModelModule = module {
    viewModelOf(::ProductoViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, dataModule, domainModule, viewModelModule)
}
```

#### Android: `androidApp/.../MainApplication.kt`
```kotlin
package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import pe.edu.upeu.pharmamobil.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
        }
    }
}
```

#### iOS: `iosApp/iosApp/iOSApp.swift`
```swift
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        PlatformModule_iosKt.initKoinIos()
    }
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

#### Consumo en Compose: `shared/.../App.kt`
```kotlin
KoinContext {
    PharmaMobilTheme(darkTheme = darkTheme) {
        // Enrutamiento desacoplado:
        when (pantallaActual) {
            is Screen.Productos -> {
                val viewModel = koinViewModel<ProductoViewModel>()
                ProductoScreen(viewModel = viewModel)
            }
            // Otros destinos...
        }
    }
}
```

---

## 5. CATÁLOGO DE VERSIONES DE GRADLE (`gradle/libs.versions.toml`)

```toml
[versions]
agp = "9.0.1"
android-compileSdk = "36"
android-minSdk = "24"
android-targetSdk = "36"
androidx-activity = "1.13.0"
androidx-appcompat = "1.7.1"
androidx-core = "1.19.0"
androidx-lifecycle = "2.11.0-beta01"
composeMultiplatform = "1.11.1"
kotlin = "2.4.10"
material3 = "1.11.0-alpha07"
koin = "4.0.2"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

---

## 6. GUÍA DE RESOLUCIÓN DE INCIDENCIAS (TROUBLESHOOTING)

1. **Incompatibilidad de AGP 9.0.1 en Android Studio:**
   * Si Android Studio arroja `"The project is using an incompatible version (AGP 9.0.1)..."`, la solución real y definitiva es actualizar Android Studio a la versión oficial 2025.1+ (Meerkat Feature Drop). Nunca degradar a AGP 8.x si el proyecto ya usa `com.android.kotlin.multiplatform.library`, ya que sus APIs internas son incompatibles.
2. **Emulador Android bloqueado (`process 16820 already running`):**
   * Cerrar los procesos colgados en PowerShell: `Get-Process *qemu* | Stop-Process -Force`.
   * Borrar los archivos de bloqueo residuales: `Remove-Item -Path "$env:USERPROFILE\.android\avd\Medium_Phone.avd\*.lock" -Force`.
3. **Fusión de ramas en Git (`main` vs `master`):**
   * Si tienes conflicto entre `main` y `master`, unifica en una sola:
     ```bash
     git branch -m main master
     git push origin --delete main
     git branch -u origin/master master
     ```
4. **Falsos positivos de compilación / Caché desincronizada:**
   * Si Android Studio marca archivos en rojo en el editor que sí compilan en terminal: Menú **File $\rightarrow$ Sync Project with Gradle Files** o **File $\rightarrow$ Invalidate Caches $\rightarrow$ Restart**.

---

## 7. COMANDOS ÚTILES PARA EL SIGUIENTE PROYECTO

```powershell
# Ejecutar todas las pruebas unitarias del módulo compartido:
.\gradlew.bat :shared:allTests

# Ejecutar pruebas unitarias en host Android:
.\gradlew.bat :shared:testAndroidHostTest

# Compilar APK de Android:
.\gradlew.bat :androidApp:assembleDebug

# Instalar y arrancar APK en el emulador:
.\gradlew.bat :androidApp:installDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n pe.edu.upeu.pharmamobil/.MainActivity
```

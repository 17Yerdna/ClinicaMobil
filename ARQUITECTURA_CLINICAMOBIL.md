# GUÍA MAESTRA DE APRENDIZAJE Y ARQUITECTURA: CLINICAMOBIL

> **Documento integral de contexto, arquitectura técnica, código de referencia, patrones de concurrencia y pruebas automatizadas** desarrollado para servir como base de conocimiento y contexto de inicio para futuros proyectos multiplataforma con **Kotlin Multiplatform (KMP)**, **Compose Multiplatform**, **Clean Architecture**, **MVVM reactivo con StateFlow** e **Inyección de Dependencias con Koin**.

---

## 1. RESUMEN EJECUTIVO DEL PROYECTO

* **Nombre de la Aplicación:** ClinicaMobil (Sistema de Admisión y Gestión de Salud Universitaria).
* **Institución:** Universidad Peruana Unión (UPeU) – Escuela Profesional de Ingeniería de Sistemas.
* **Estudiante / Desarrollador:** Andrey Mestanza | **GitHub:** `https://github.com/17Yerdna/ClinicaMobil.git`
* **Rama Principal de Desarrollo:** `feature/clean-mvvm`
* **Puntaje Obtenido:** 20 / 20 puntos (Cumplimiento total de rúbricas prácticas y teóricas).
* **Ecosistema Tecnológico:**
  * **Lenguaje:** Kotlin 2.4.10 (Multiplatform `commonMain`, `androidMain`, `iosMain`).
  * **UI Multiplataforma:** Jetpack / JetBrains Compose Multiplatform 1.11.1 con Material 3 (`material3:1.11.0-alpha07`).
  * **Inyección de Dependencias:** Koin Core & Compose 4.0.2.
  * **Concurrencia:** Kotlinx Coroutines 1.10.1 con `Mutex` y `StateFlow`.
  * **Gestión de Versiones:** Gradle Version Catalogs (`gradle/libs.versions.toml`).
  * **Testing:** `kotlin-test`, `kotlinx-coroutines-test` (con `UnconfinedTestDispatcher`).

---

## 2. PRINCIPIOS DE DISEÑO Y ARQUITECTURA APLICADA

ClinicaMobil implementa una arquitectura en capas desacopladas fundamentada en los principios de **Clean Architecture** y el patrón **MVVM** con **Flujo Unidireccional de Datos (UDF)**.

### 2.1. Regla de Dependencia Estricta
Las capas internas representan el núcleo de negocio y **jamás** conocen la existencia de las capas externas:
```
  [ Presentación (Compose UI + ViewModel) ] ──▶ [ Dominio (Modelos + Casos de Uso) ] ◀── [ Datos (Repositorios) ]
```
* **Dominio (`domain`):** Es Kotlin puro. **No importa** bibliotecas de UI (Compose), sistemas operativos (Android/iOS), red (Ktor) ni contenedores de DI (Koin). Contiene las entidades, las invariantes y los contratos de repositorio.
* **Presentación (`presentation`):** Consume casos de uso y modelos de dominio. **Tiene prohibido importar la capa de datos** (penalización crítica en evaluación).
* **Datos (`data`):** Implementa las interfaces de repositorio definidas en dominio. Toda gestión de persistencia (en memoria, Room, SQLDelight o Ktor) queda aislada en esta capa.

### 2.2. Diagrama de Flujo Unidireccional de Datos (UDF)
```
       ┌────────────────────────────────────────────────────────┐
       │                 Compose Screen                         │
       │  (PacienteScreen / MedicoScreen / InicioScreen)        │
       └────────────────────────┬──────────────────────▲────────┘
                     Eventos UI │                      │ StateFlow<UiState>
                 (Clicks, Text) │                      │ (Renderiza Fases)
                                ▼                      │
       ┌───────────────────────────────────────────────┴────────┐
       │                   ViewModel                            │
       │        (PacienteViewModel / MedicoViewModel)           │
       └────────────────────────┬───────────────────────────────┘
                         Invoca │ operator fun invoke()
                                ▼
       ┌────────────────────────────────────────────────────────┐
       │                   Caso de Uso                          │
       │    (RegistrarPacienteUseCase / ListarPacientesUseCase)  │
       │         * Valida entradas y retorna Result<T>          │
       └────────────────────────┬───────────────────────────────┘
                Delega contrato │ PacienteRepository (interface)
                                ▼
       ┌────────────────────────────────────────────────────────┐
       │             Repositorio en Datos                       │
       │        (PacienteRepositorioEnMemoria)                  │
       │    * Concurrencia segura con Mutex.withLock            │
       │    * Simulación de latencia con delay(300..800)        │
       └────────────────────────────────────────────────────────┘
```

---

## 3. COMPONENTES Y DECISIONES TÉCNICAS CLAVE

### 3.1. Dominio Puro con Invariantes Fuertes
Las entidades se modelan como `data class` inmutables cuyo estado válido se garantiza desde su instanciación mediante bloques `init { require(...) }`.

```kotlin
package pe.edu.upeu.clinicamobil.domain.model

data class Paciente(
    val id: Long = 0L,
    val nombre: String,
    val dni: String,
    val edad: Int,
    val peso: Double
) {
    init {
        require(nombre.isNotBlank()) { "El nombre es obligatorio" }
        require(dni.isNotBlank()) { "El DNI es obligatorio" }
        require(dni.matches(Regex("^[0-9]{8}$"))) { "El DNI debe tener 8 dígitos" }
        require(edad in 0..EDAD_MAXIMA) { "La edad debe estar entre 0 y 120" }
        require(!peso.isNaN() && !peso.isInfinite()) { "El peso debe ser un número válido" }
        require(peso > 0) { "El peso debe ser mayor a 0" }
    }

    /** Regla de negocio derivada: pediátrico si es menor de 18 años */
    val esPediatrico: Boolean
        get() = edad < EDAD_ADULTO

    companion object {
        const val EDAD_ADULTO = 18
        const val EDAD_MAXIMA = 120
    }
}
```

### 3.2. Casos de Uso y Validación Auditable (Single Source of Truth)
Para no dispersar las reglas de validación en los formularios ni en los ViewModels, los casos de uso reciben cadenas crudas (`String`), sanitizan con `.trim()`, recopilan errores estructurados por campo (`ErroresDePaciente`) y lanzan excepciones tipadas (`PacienteInvalidoException`).

```kotlin
package pe.edu.upeu.clinicamobil.domain.usecase

data class ErroresDePaciente(
    val nombre: String? = null,
    val dni: String? = null,
    val edad: String? = null,
    val peso: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || dni != null || edad != null || peso != null
}

class PacienteInvalidoException(val errores: ErroresDePaciente) : IllegalArgumentException("Datos de paciente inválidos")
```

### 3.3. Helper de Corrutinas `resultadoDe` con Relanzamiento de Cancelación
En Kotlin Coroutines, capturar genéricamente `catch (e: Exception)` o `catch (e: Throwable)` rompe la cancelación cooperativa de trabajos (*Job cancellation*). Es obligatorio relanzar `CancellationException`:

```kotlin
package pe.edu.upeu.clinicamobil.domain.usecase

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> resultadoDe(bloque: () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e // Preserva la cancelación de corrutinas en Compose y ViewModels
} catch (e: Throwable) {
    Result.failure(e)
}
```

### 3.4. Concurrencia en Memoria con `Mutex`
Para evitar condiciones de carrera (*race conditions*) cuando múltiples corrutinas leen o escriben simultáneamente en colecciones en memoria, se utiliza `kotlinx.coroutines.sync.Mutex` en lugar de bloques `synchronized` de Java (los cuales no son compatibles con KMP ni con funciones de suspensión):

```kotlin
package pe.edu.upeu.clinicamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.clinicamobil.domain.model.Paciente
import pe.edu.upeu.clinicamobil.domain.repository.PacienteRepository
import kotlin.random.Random

class PacienteRepositorioEnMemoria(
    datosIniciales: List<Paciente> = defaultPacientes
) : PacienteRepository {

    private val mutex = Mutex()
    private val padron = mutableListOf<Paciente>().apply { addAll(datosIniciales) }
    private var nextId = (datosIniciales.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(paciente: Paciente): Paciente {
        simularLatencia()
        return mutex.withLock {
            val conId = paciente.copy(id = nextId++)
            padron.add(conId)
            conId
        }
    }

    override suspend fun listar(): List<Paciente> {
        simularLatencia()
        return mutex.withLock { padron.toList() }
    }

    private suspend fun simularLatencia() {
        delay(Random.nextLong(300, 801))
    }

    companion object {
        val defaultPacientes = listOf(
            Paciente(id = 1L, nombre = "Carlos Mendoza", dni = "12345678", edad = 34, peso = 70.5),
            Paciente(id = 2L, nombre = "Ana Gómez", dni = "87654321", edad = 8, peso = 25.0)
        )
    }
}
```

### 3.5. Formateo Numérico Manual en `commonMain`
Dado que `java.lang.String.format` o `String.format("%.1f")` no existen de forma multiplataforma nativa en `commonMain`, el formateo a un decimal exacto se construye mediante aritmética de enteros:

```kotlin
fun formatearUnDecimal(valor: Double): String {
    val totalDecimas = (valor * 10.0 + if (valor >= 0) 0.5 else -0.5).toLong()
    val parteEntera = totalDecimas / 10
    val parteDecimal = kotlin.math.abs(totalDecimas % 10)
    return "$parteEntera.$parteDecimal"
}

// Ejemplo de uso:
// 70.5  -> "70.5 kg"
// 70.0  -> "70.0 kg"
// 9.2   -> "9.2 kg"
```

### 3.6. Fases Excluyentes en `UiState` y Prevención de Doble Toque
El estado de la vista modela las 4 fases del ciclo de vida asíncrono con una `sealed interface`:

```kotlin
sealed interface FasePacientes {
    data object Cargando : FasePacientes
    data object SinPacientes : FasePacientes
    data class ConPacientes(val pacientes: List<PacienteUi>) : FasePacientes
    data class Error(val mensaje: String) : FasePacientes
}

data class PacienteUiState(
    val fase: FasePacientes = FasePacientes.Cargando,
    val formulario: FormularioPaciente = FormularioPaciente(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null
)
```

En el `ViewModel`:
* Los cambios en cada campo (`onNombreChange`, etc.) eliminan de inmediato el error de dicho campo sin necesidad de revalidar toda la entidad.
* Al presionar registrar, se verifica `if (_uiState.value.registrando) return` para prevenir peticiones duplicadas ante pulsaciones dobles del usuario.
* Si el caso de uso falla con `PacienteInvalidoException`, los errores se vuelcan al formulario sin pasar la fase a `Error` (la lista sigue visible).

### 3.7. Navegación con `rememberSaveable` y `Saver`
Para asegurar que la pantalla actual y la posición del usuario sobrevivan a cambios de configuración como la **rotación de pantalla**:

```kotlin
sealed class Screen(val ruta: String, val titulo: String, val icono: ImageVector) {
    data object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    data object Pacientes : Screen("pacientes", "Pacientes", Icons.Default.Person)
    data object Medicos : Screen("medicos", "Médicos", Icons.Default.MedicalServices)
    data object Historias : Screen("historias", "Historias Clínicas", Icons.AutoMirrored.Filled.Assignment)

    companion object {
        val DESTINOS: List<Screen> = listOf(Inicio, Pacientes, Medicos, Historias)
        fun desdeRuta(ruta: String?): Screen = when (ruta) {
            Pacientes.ruta -> Pacientes
            Medicos.ruta -> Medicos
            Historias.ruta -> Historias
            else -> Inicio
        }
    }
}

val ScreenSaver: Saver<Screen, String> = Saver(
    save = { it.ruta },
    restore = { Screen.desdeRuta(it) }
)

// Uso en Composable raíz:
var pantallaActual by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf(Screen.Inicio) }
```

### 3.8. Inyección de Dependencias con Koin Multiplatform
Koin centraliza el cableado respetando las reglas de Clean Architecture:
* Repositorios registrados **por su interfaz** como `single` (garantiza una única instancia compartida con persistencia y Mutex en memoria).
* Casos de uso registrados como `factoryOf` (nueva instancia por invocación, sin retención de estado).
* ViewModels registrados como `viewModelOf` consumidos mediante `koinViewModel<T>()`.
* Aislamiento: Ningún archivo fuera de `di` conoce o referencia las clases concretas `*EnMemoria`.

```kotlin
package pe.edu.upeu.clinicamobil.di

val dataModule = module {
    single<PacienteRepository> { PacienteRepositorioEnMemoria() }
    single<MedicoRepository> { MedicoRepositorioEnMemoria() }
}

val domainModule = module {
    factoryOf(::RegistrarPacienteUseCase)
    factoryOf(::RegistrarMedicoUseCase)
    factoryOf(::ListarPacientesUseCase)
    factoryOf(::ListarMedicosUseCase)
}

val presentationModule = module {
    viewModelOf(::PacienteViewModel)
    viewModelOf(::MedicoViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, dataModule, domainModule, presentationModule)
}
```

---

## 4. IDENTIDAD VISUAL MATERIAL 3 (THEMING CLÍNICO)

Se diseñó una identidad visual médica personalizada en **Azul Marino Verdoso Claro** combinado con superficies cálidas en **Crema**, cubriendo al 100% los roles de color de Material 3 (`surfaceContainer*`):

* **Primario:** `Color(0xFF007385)` (Light) / `Color(0xFF53D7EE)` (Dark).
* **Superficies Crema:**
  * `SurfaceLight`: `Color(0xFFFBF8F2)`
  * `BackgroundLight`: `Color(0xFFFDFBF7)`
  * `SurfaceContainerLight`: `Color(0xFFF0EDE7)`
  * `SurfaceContainerHighLight`: `Color(0xFFEAE7E1)`
* **Componentes Reutilizables:**
  * `ValidatedTextField`: Integra `OutlinedTextField` con soporte dinámico para errores y tipos de teclado numérico/decimal.
  * `MensajeExito`: Notificación visual de confirmación de registro.
  * `EstadoVacio`: Representa listas vacías, pantallas en construcción (RF-05) y estados de error con botón "Reintentar".

---

## 5. ESTRATEGIA DE PRUEBAS AUTOMATIZADAS (29 TESTS VERDES)

Se implementó una suite completa de pruebas unitarias en `shared/src/commonTest` utilizando **Fakes** en lugar de mocks pesados:
* `FakePacienteRepository` y `FakeMedicoRepository`: Sin delays, con soporte para simular fallos configurables (`debeFallar = true`).
* **Pruebas de ViewModel:** Utilizan `UnconfinedTestDispatcher` mediante `Dispatchers.setMain(...)` en `@BeforeTest` y `Dispatchers.resetMain()` en `@AfterTest` para ejecutar corrutinas de manera inmediata y síncrona en las aserciones.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class PacienteViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun arranca_en_SinPacientes() {
        val fakeRepo = FakePacienteRepository(emptyList())
        val viewModel = PacienteViewModel(
            registrarPaciente = RegistrarPacienteUseCase(fakeRepo),
            listarPacientes = ListarPacientesUseCase(fakeRepo)
        )
        assertTrue(viewModel.uiState.value.fase is FasePacientes.SinPacientes)
    }
}
```

---

## 6. CONFIGURACIÓN DE GRADLE Y CATÁLOGO DE VERSIONES

### `gradle/libs.versions.toml`
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
composeMaterialIconsExtended = "1.7.3"
junit = "4.13.2"
kotlin = "2.4.10"
material3 = "1.11.0-alpha07"
koin = "4.0.2"
kotlinx-coroutines = "1.10.1"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }

kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "material3" }
compose-material-icons-extended = { group = "org.jetbrains.compose.material", name = "material-icons-extended", version.ref = "composeMaterialIconsExtended" }
```

### Justificación de Dependencias `api` en `shared/build.gradle.kts`:
```kotlin
// Se expone como api para permitir que las plataformas cliente (Android e iOS) accedan al contexto de Koin e inicien el contenedor de dependencias
api(libs.koin.core)

// Se expone como api para que la capa de UI multiplataforma y sus extensiones puedan proveer el KoinContext globalmente
api(libs.koin.compose)

// Se expone como api para que los puntos de entrada visuales puedan resolver instancias de ViewModel con koinViewModel()
api(libs.koin.compose.viewmodel)
```

---

## 7. PREGUNTAS FRECUENTES Y GUÍA DE TROUBLESHOOTING

1. **Error al resolver `material-icons-extended`:**
   * *Causa:* En Compose Multiplatform, la biblioteca de iconos extendidos se congeló en la versión `1.7.3`. Intentar usar la versión del plugin (ej. `1.11.1`) arrojará error 404 en Maven Central.
   * *Solución:* Fijar `composeMaterialIconsExtended = "1.7.3"` explícitamente en `libs.versions.toml`.
2. **Doble pulsación en botones de registro:**
   * *Solución:* Proteger la función en el ViewModel con `if (_uiState.value.registrando) return` y deshabilitar el botón en la UI asignando `enabled = !uiState.registrando`.
3. **Pérdida de estado al rotar la pantalla:**
   * *Solución:* No almacenar variables de negocio en la vista con `remember { mutableStateOf(...) }`. Todo el estado del formulario debe vivir en el `StateFlow` del `ViewModel`, y la navegación raíz debe usar `rememberSaveable` con un `Saver` personalizado.
4. **Falsos positivos de compilación o caché residual:**
   * *Solución:* Ejecutar con `./gradlew.bat --stop` y regenerar con `./gradlew.bat :shared:assemble`.

---

## 8. COMANDOS ÚTILES PARA EL SIGUIENTE PROYECTO

```powershell
# Ejecutar todas las pruebas unitarias del host:
.\gradlew.bat :shared:testAndroidHostTest

# Compilar APK de depuración para Android:
.\gradlew.bat :androidApp:assembleDebug

# Compilar framework para simulador de iOS:
.\gradlew.bat :shared:compileKotlinIosSimulatorArm64

# Instalar y ejecutar APK en el emulador Android activo:
.\gradlew.bat :androidApp:installDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n pe.edu.upeu.clinicamobil/.MainActivity

# Tomar captura de pantalla por ADB y guardarla:
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" exec-out screencap -p > "docs/CAPTURAS/pantalla.png"
```

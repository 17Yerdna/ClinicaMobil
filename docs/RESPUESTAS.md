# Respuestas Teóricas y Evidencias de Pruebas — ClinicaMobil

**Estudiante:** Andrey Mestanza  
**Curso:** Desarrollo de Aplicaciones Móviles Multiplataforma  
**Proyecto:** ClinicaMobil  
**Rama:** `feature/clean-mvvm`

---

## Parte II — Preguntas Teóricas

### Pregunta 1
**Cuando los datos se centralicen en el servidor del área de salud, ¿qué archivos de tu proyecto cambian y cuáles quedan intactos? Nómbralos y justifica con la regla de dependencia entre capas.**

Al centralizar los datos en el servidor del área de salud, los únicos archivos que cambian son los pertenecientes a la capa de datos (`data/repository/PacienteRepositorioRemoto.kt`, `data/repository/MedicoRepositorioRemoto.kt`, posibles fuentes de red con Ktor o servicios API) y el módulo de inyección de dependencias `di/AppModule.kt` en su bloque `dataModule` para enlazar las interfaces con las nuevas implementaciones remotas. Por el contrario, permanecen completamente intactos todos los archivos de la capa de dominio (`domain/model/Paciente.kt`, `domain/model/Medico.kt`, `domain/model/HistoriaClinica.kt`, `domain/model/Atencion.kt`, `domain/model/EstadoHistoria.kt`, `domain/repository/PacienteRepository.kt`, `domain/repository/MedicoRepository.kt`, y todos los casos de uso en `domain/usecase/`) así como todos los archivos de la capa de presentación (`presentation/paciente/*`, `presentation/medico/*`, `presentation/inicio/*`, `presentation/navigation/*`, etc.). Esto se justifica mediante la **Regla de Dependencia de Clean Architecture**, la cual establece que las capas internas (Dominio) representan las políticas de negocio de alto nivel y jamás deben depender de detalles volátiles de infraestructura o mecanismos de persistencia externos (Datos); las capas externas dependen hacia adentro de los contratos abstractos (interfaces), permitiendo sustituir la fuente de datos sin que la lógica ni las interfaces visuales se vean afectadas.

---

### Pregunta 2
**La dirección exige que cada regla de validación viva en un único lugar auditable. ¿En qué clase de tu proyecto vive la regla "el DNI debe tener 8 dígitos"? ¿Por qué no debe repetirse en PacienteScreen ni en PacienteViewModel, y qué pasaría si el modelo Paciente la relajara?**

La regla *"el DNI debe tener 8 dígitos"* vive formalmente en la clase de caso de uso `RegistrarPacienteUseCase` (en `domain/usecase/RegistrarPacienteUseCase.kt`), donde se evalúa sobre la entrada del usuario mediante la expresión regular `^[0-9]{8}$`, y se refuerza como invariante estructural en el bloque `init { require(...) }` de la entidad de dominio `Paciente`. No debe repetirse en `PacienteScreen` ni en `PacienteViewModel` para cumplir con el principio de **Única Fuente de Verdad** (*Single Source of Truth*), garantizando que las reglas clínicas y legales del sistema de salud residan en un único punto centralizado y auditable, de modo que cualquier cambio en la regulación afecte por igual a cualquier cliente (Android, iOS o futura web) sin dispersar lógica en capas visuales. Si el modelo `Paciente` relajara dicha regla (por ejemplo, permitiendo cadenas de cualquier longitud o formato), se abriría una vulnerabilidad arquitectónica en la cual una instancia de `Paciente` con datos corruptos o incompletos podría crearse válidamente en memoria a través de otra fuente o caso de uso futuro, violando la integridad y consistencia del padrón de salud universitario.

---

### Pregunta 3
**Explica qué observaría el personal de admisión si PacienteRepository estuviera registrado en Koin como factory en vez de single, y por qué.**

Si `PacienteRepository` estuviera registrado en Koin como `factory` en lugar de `single`, el personal de admisión observaría que **los pacientes recién registrados nunca aparecen en el listado del padrón (o desaparecen de inmediato tras pulsar registrar)**, mostrando siempre un estado vacío o desincronizado. Esto ocurre porque el calificador `factory` instruye a Koin a crear y retornar una instancia completamente nueva e independiente del repositorio cada vez que se solicita la inyección; por lo tanto, `RegistrarPacienteUseCase` recibiría una instancia en memoria donde guardaría al paciente, mientras que `ListarPacientesUseCase` recibiría una instancia distinta con una lista en memoria totalmente separada y vacía, y cualquier otra pantalla o caso de uso operaría sobre colecciones huérfanas en memoria. Al registrarlo como `single`, Koin garantiza que todos los componentes compartan la misma y única instancia del repositorio en memoria, manteniendo la persistencia y la protección de concurrencia mediante el `Mutex` común.

---

## Salida de Pruebas Automatizadas

```text
> Task :shared:compileAndroidMain UP-TO-DATE
> Task :shared:bundleAndroidMainClassesToRuntimeJar UP-TO-DATE
> Task :shared:bundleAndroidMainClassesToCompileJar UP-TO-DATE
> Task :shared:processAndroidMainJavaRes UP-TO-DATE
> Task :shared:compileAndroidHostTest UP-TO-DATE
> Task :shared:testAndroidHostTest

BUILD SUCCESSFUL in 14s
32 actionable tasks: 1 executed, 31 up-to-date
Configuration cache entry reused.
```

### Resumen de Ejecución del Reporte HTML de Tests:
* **Total de Pruebas:** 29 tests
* **Fallos:** 0 failures
* **Ignorados:** 0 ignored
* **Tasa de Éxito:** 100%

#### Detalle de Suites Ejecutadas:
1. `pe.edu.upeu.clinicamobil.PacienteTest` (4 pruebas) — Invariantes de nombre, rango de edad, regla pediátrica (<18) y paciente válido.
2. `pe.edu.upeu.clinicamobil.AtencionTest` (3 pruebas) — Duración mínima (1 min), duración máxima (120 min) y cálculo exacto de costo ($30 \times 2.50 = 75.0$).
3. `pe.edu.upeu.clinicamobil.RegistrarPacienteUseCaseTest` (7 pruebas) — Paciente válido, nombre obligatorio, DNI obligatorio, DNI 8 dígitos, edad obligatoria/entero/rango, peso obligatorio/válido/>0, asignación correlativa de ID y manejo de fallos del repositorio.
4. `pe.edu.upeu.clinicamobil.RegistrarMedicoUseCaseTest` (4 pruebas) — Colegiatura 4 dígitos rechazada, especialidad <3 caracteres rechazada, especialidad en blanco convertida a null, y médico válido con especialidad.
5. `pe.edu.upeu.clinicamobil.PacienteRepositorioEnMemoriaTest` (2 pruebas) — Asignación de IDs correlativos desde 1 y listado en orden estricto de registro.
6. `pe.edu.upeu.clinicamobil.PacienteViewModelTest` (6 pruebas) — Inicio en SinPacientes, formateo de línea secundaria (`34 años · 70.5 kg`), transición a Error si el repositorio falla, errores de validación dirigidos al formulario sin afectar la fase, y limpieza del formulario tras registro exitoso con recarga.
7. `pe.edu.upeu.clinicamobil.AppModuleTest` (3 pruebas) — Resolución de PacienteRepository como PacienteRepositorioEnMemoria, verificación de singleton (`single`), y resolución exitosa de los 4 casos de uso.

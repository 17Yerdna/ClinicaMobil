package pe.edu.upeu.clinicamobil

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.clinicamobil.presentation.historia.HistoriasScreen
import pe.edu.upeu.clinicamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.clinicamobil.presentation.medico.MedicoScreen
import pe.edu.upeu.clinicamobil.presentation.medico.MedicoViewModel
import pe.edu.upeu.clinicamobil.presentation.navigation.Screen
import pe.edu.upeu.clinicamobil.presentation.navigation.ScreenSaver
import pe.edu.upeu.clinicamobil.presentation.paciente.PacienteScreen
import pe.edu.upeu.clinicamobil.presentation.paciente.PacienteViewModel
import pe.edu.upeu.clinicamobil.presentation.theme.ClinicaMobilTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val systemDark = isSystemInDarkTheme()
    var modoOscuro by rememberSaveable { mutableStateOf(systemDark) }

    KoinContext {
        ClinicaMobilTheme(darkTheme = modoOscuro) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // pantallaActual sobrevive a la rotación gracias al ScreenSaver
            var pantallaActual by rememberSaveable(stateSaver = ScreenSaver) {
                mutableStateOf(Screen.Inicio)
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp)
                    ) {
                        // Cabecera de marca del centro de salud
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ClinicaMobil",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Centro de Salud Universitario",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Menú lateral alimentado por una sola lista DESTINOS
                        Screen.DESTINOS.forEach { destino ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = destino.icono,
                                        contentDescription = destino.titulo
                                    )
                                },
                                label = { Text(destino.titulo) },
                                selected = pantallaActual == destino,
                                onClick = {
                                    pantallaActual = destino
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider()

                        // Interruptor de modo oscuro al pie del menú
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Modo oscuro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = modoOscuro,
                                onCheckedChange = { modoOscuro = it }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = pantallaActual.titulo,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menú de navegación"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                ) { innerPadding ->
                    when (pantallaActual) {
                        is Screen.Inicio -> {
                            InicioScreen(
                                onNavegar = { destino -> pantallaActual = destino },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Pacientes -> {
                            val viewModel = koinViewModel<PacienteViewModel>()
                            PacienteScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Medicos -> {
                            val viewModel = koinViewModel<MedicoViewModel>()
                            MedicoScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Historias -> {
                            HistoriasScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

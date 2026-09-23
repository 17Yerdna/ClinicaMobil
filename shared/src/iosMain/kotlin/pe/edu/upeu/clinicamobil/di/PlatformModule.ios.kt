package pe.edu.upeu.clinicamobil.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Implementación actual del módulo de plataforma en iOS.
 */
actual val platformModule: Module = module {
}

/**
 * Función puente invocable desde iOS (iOSApp.swift) para inicializar Koin.
 */
fun initKoinIos() = initKoin()

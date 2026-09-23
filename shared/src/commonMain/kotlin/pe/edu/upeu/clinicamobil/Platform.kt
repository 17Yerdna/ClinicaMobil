package pe.edu.upeu.clinicamobil

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

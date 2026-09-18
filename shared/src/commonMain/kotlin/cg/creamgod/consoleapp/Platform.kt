package cg.creamgod.consoleapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
package cg.creamgod.consoleapp

import java.util.Locale

enum class OSType {
    WINDOWS, MACOS, LINUX, OTHER
}

object OSDetector {
    val currentOS: OSType by lazy {
        val osName = System.getProperty("os.name", "generic").lowercase(Locale.ENGLISH)
        when {
            "win" in osName -> OSType.WINDOWS
            "mac" in osName || "darwin" in osName -> OSType.MACOS
            "nux" in osName || "nix" in osName || "aix" in osName -> OSType.LINUX
            else -> OSType.OTHER
        }
    }

    // 提供快速布林檢查屬性
    val isWindows: Boolean get() = currentOS == OSType.WINDOWS
    val isMac: Boolean get() = currentOS == OSType.MACOS
    val isLinux: Boolean get() = currentOS == OSType.LINUX
}
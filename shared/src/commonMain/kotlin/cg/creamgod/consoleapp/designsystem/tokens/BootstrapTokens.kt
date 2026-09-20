package cg.creamgod.consoleapp.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Bootstrap-compatible naming for layout decisions. Values stay Compose-native
 * so the same components can render on JVM, JS, and Wasm.
 */
object BootstrapTokens {
    object Spacing {
        val zero = 0.dp
        val one = 4.dp
        val two = 8.dp
        val three = 16.dp
        val four = 24.dp
        val five = 48.dp
    }

    object Radius {
        val small = 4.dp
        val medium = 6.dp
        val large = 8.dp
        val pill = 999.dp
    }

    object Breakpoint {
        val small = 576.dp
        val medium = 768.dp
        val large = 992.dp
        val extraLarge = 1200.dp
        val extraExtraLarge = 1400.dp
    }
}

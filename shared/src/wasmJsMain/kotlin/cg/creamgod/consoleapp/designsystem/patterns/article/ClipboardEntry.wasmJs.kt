package cg.creamgod.consoleapp.designsystem.patterns.article

import androidx.compose.ui.platform.ClipEntry

internal actual fun createPlainTextClipEntry(text: String): ClipEntry =
    ClipEntry.withPlainText(text)

package cg.creamgod.consoleapp.designsystem.patterns.article

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

internal actual fun createPlainTextClipEntry(text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText(text, text))

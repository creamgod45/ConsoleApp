package cg.creamgod.consoleapp

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val filePicker = BrowserFilePicker()
    val questionApi = WebQuestionApi(
        "http://localhost:8000/api/upload",
        filePicker = filePicker
    )

    ComposeViewport {
        WithFontResourcesLoaded {
            App(
                submitQuestion = questionApi::submit,
                filePicker = filePicker
            )
        }
    }
}

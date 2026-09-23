package cg.creamgod.consoleapp

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val filePicker = remember { DesktopFilePicker() }
    val questionApi = remember { DesktopQuestionApi("http://localhost:8000/api/upload") }
    val mailApi = remember { DesktopMailApi("http://localhost:8000/api/faker/mail") }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ConsoleApp",
        state = rememberWindowState(width = 1024.dp, height = 768.dp, placement = WindowPlacement.Maximized),
    ) {
        App(
            submitQuestion = questionApi::submit,
            loadMails = mailApi::load,
            filePicker = filePicker,
            dialogHost = { filePicker.Host() },
        )
    }
}

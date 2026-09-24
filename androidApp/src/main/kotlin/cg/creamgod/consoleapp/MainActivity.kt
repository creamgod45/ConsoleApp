package cg.creamgod.consoleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val filePicker = AndroidFilePicker(this)
        val questionApi = AndroidQuestionApi(
            endpoint = "${BuildConfig.API_BASE_URL}/api/upload",
            contentResolver = contentResolver,
        )
        val mailApi = AndroidMailApi(
            endpoint = "${BuildConfig.API_BASE_URL}/api/faker/mail",
        )

        setContent {
            App(
                submitQuestion = questionApi::submit,
                loadMails = mailApi::load,
                filePicker = filePicker,
            )
        }
    }
}

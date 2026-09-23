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

        setContent {
            // 目前用 App() 的預設參數：Android 版還沒有自己的 filePicker / 上傳 / 郵件實作，
            // 那幾個畫面會落到 shared 的預設行為。要接上時比照 desktopApp、webApp 的 main
            // 建立實作後傳進來即可。
            App()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    App()
}

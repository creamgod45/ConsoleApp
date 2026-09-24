package cg.creamgod.consoleapp

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun rememberPlatformCameraController(): PlatformCameraController = remember {
    PlatformCameraController(CameraCapabilities(platformName = "JavaScript Browser", hasCamera = false))
}

@Composable
actual fun PlatformCameraPreview(controller: PlatformCameraController, modifier: Modifier) {
    Box(modifier)
}

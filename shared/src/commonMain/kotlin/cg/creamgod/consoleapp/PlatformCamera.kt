package cg.creamgod.consoleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

data class CameraCapabilities(
    val platformName: String,
    val hasCamera: Boolean,
    val canRecordVideo: Boolean = false,
    val canSwitchCamera: Boolean = false,
    val canControlIso: Boolean = false,
    val canControlBrightness: Boolean = false,
    val rendersEffects: Boolean = false,
    val isoRange: ClosedFloatingPointRange<Float> = 100f..3200f,
    val brightnessRange: ClosedFloatingPointRange<Float> = -2f..2f,
)

class PlatformCameraController internal constructor(initialCapabilities: CameraCapabilities) {
    internal var platformHandle: Any? = null
    var capabilities by mutableStateOf(initialCapabilities)
        internal set
    var isReady by mutableStateOf(false)
        internal set
    var permissionDenied by mutableStateOf(false)
        internal set
    var isRecording by mutableStateOf(false)
        internal set
    var iso by mutableStateOf(100f)
        internal set
    var brightness by mutableStateOf(0f)
        internal set
    var chroma by mutableStateOf(1f)
        internal set
    var filter by mutableStateOf(CameraFilter.NONE)
        internal set
    var statusMessage by mutableStateOf<String?>(null)
        internal set

    internal var onRequestPermission: () -> Unit = {}
    internal var onSwitchCamera: () -> Unit = {}
    internal var onIsoChanged: (Float) -> Unit = {}
    internal var onBrightnessChanged: (Float) -> Unit = {}
    internal var onTakePhoto: () -> Unit = {}
    internal var onToggleRecording: () -> Unit = {}

    fun requestPermission() = onRequestPermission()
    fun switchCamera() = onSwitchCamera()
    fun takePhoto() = onTakePhoto()
    fun toggleRecording() = onToggleRecording()
    fun clearStatus() { statusMessage = null }

    fun setIso(value: Float) {
        iso = value.coerceIn(capabilities.isoRange.start, capabilities.isoRange.endInclusive)
        onIsoChanged(iso)
    }

    fun setBrightness(value: Float) {
        brightness = value.coerceIn(capabilities.brightnessRange.start, capabilities.brightnessRange.endInclusive)
        onBrightnessChanged(brightness)
    }

    fun setChroma(value: Float) { chroma = value.coerceIn(0f, 2f) }
    fun setFilter(value: CameraFilter) { filter = value }
}

@Composable
expect fun rememberPlatformCameraController(): PlatformCameraController

@Composable
expect fun PlatformCameraPreview(controller: PlatformCameraController, modifier: Modifier = Modifier)

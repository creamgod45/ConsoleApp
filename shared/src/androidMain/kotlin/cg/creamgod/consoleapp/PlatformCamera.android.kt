package cg.creamgod.consoleapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalCamera2Interop::class)
@Composable
actual fun rememberPlatformCameraController(): PlatformCameraController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        PlatformCameraController(
            CameraCapabilities(
                platformName = "Android",
                hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
                canRecordVideo = true,
                canSwitchCamera = true,
                canControlBrightness = true,
                rendersEffects = true,
            ),
        )
    }
    val session = remember(context, lifecycleOwner, controller) {
        AndroidCameraSession(context, lifecycleOwner, controller)
    }
    controller.platformHandle = session

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val cameraGranted = result[Manifest.permission.CAMERA] == true
        controller.permissionDenied = !cameraGranted
        if (cameraGranted) session.bind()
        else controller.statusMessage = "未取得相機權限"
    }
    controller.onRequestPermission = { launcher.launch(permissions) }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        controller.permissionDenied = !granted
        if (granted) session.bind() else launcher.launch(permissions)
    }
    DisposableEffect(session) { onDispose { session.close() } }

    controller.onSwitchCamera = session::switchCamera
    controller.onIsoChanged = { session.applyManualExposure() }
    controller.onBrightnessChanged = { session.applyManualExposure() }
    controller.onTakePhoto = session::takePhoto
    controller.onToggleRecording = session::toggleRecording
    return controller
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
actual fun PlatformCameraPreview(controller: PlatformCameraController, modifier: Modifier) {
    val session = controller.platformHandle as? AndroidCameraSession ?: return
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                session.attach(this)
            }
        },
        update = { session.applyPreviewEffects(it) },
        modifier = modifier,
    )
}

@ExperimentalCamera2Interop
private class AndroidCameraSession(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val controller: PlatformCameraController,
) {
    private val executor = ContextCompat.getMainExecutor(context)
    private var provider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var useFrontCamera = false

    fun attach(view: PreviewView) {
        previewView = view
        applyPreviewEffects(view)
        if (hasCameraPermission()) bind()
    }

    fun applyPreviewEffects(view: PreviewView) {
        val saturation = when (controller.filter) {
            CameraFilter.MONO -> 0f
            CameraFilter.VIVID -> controller.chroma * 1.25f
            else -> controller.chroma
        }
        val matrix = ColorMatrix().apply { setSaturation(saturation) }
        when (controller.filter) {
            CameraFilter.WARM -> matrix.postConcat(ColorMatrix(floatArrayOf(
                1.10f, 0f, 0f, 0f, 0f,
                0f, 1.02f, 0f, 0f, 0f,
                0f, 0f, .88f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )))
            CameraFilter.COOL -> matrix.postConcat(ColorMatrix(floatArrayOf(
                .88f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1.10f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )))
            else -> Unit
        }
        view.setLayerType(
            View.LAYER_TYPE_HARDWARE,
            Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) },
        )
    }

    fun bind() {
        val surfaceProvider = previewView?.surfaceProvider ?: return
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            runCatching {
                val cameraProvider = future.get()
                provider = cameraProvider
                val preview = Preview.Builder().build().also { it.surfaceProvider = surfaceProvider }
                val stillCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                val recorder = Recorder.Builder().build()
                val movieCapture = VideoCapture.withOutput(recorder)
                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    stillCapture,
                    movieCapture,
                )
                imageCapture = stillCapture
                videoCapture = movieCapture
                updateCapabilities()
                controller.isReady = true
                controller.permissionDenied = false
            }.onFailure {
                controller.isReady = false
                controller.statusMessage = "相機啟動失敗：${it.message ?: "裝置不支援目前的拍攝組合"}"
            }
        }, executor)
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun updateCapabilities() {
        val activeCamera = camera ?: return
        val info = Camera2CameraInfo.from(activeCamera.cameraInfo)
        val sensitivity = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure = activeCamera.cameraInfo.exposureState
        val evStep = exposure.exposureCompensationStep.toFloat()
        val evRange = exposure.exposureCompensationRange
        controller.capabilities = controller.capabilities.copy(
            canSwitchCamera = runCatching { provider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true }.getOrDefault(false),
            canControlIso = sensitivity != null,
            canControlBrightness = sensitivity != null || exposure.isExposureCompensationSupported,
            isoRange = sensitivity?.let { it.lower.toFloat()..it.upper.toFloat() } ?: 100f..3200f,
            brightnessRange = if (exposure.isExposureCompensationSupported) {
                (evRange.lower * evStep)..(evRange.upper * evStep)
            } else -2f..2f,
        )
        sensitivity?.let {
            controller.iso = controller.iso.coerceIn(it.lower.toFloat(), it.upper.toFloat())
        }
    }

    fun switchCamera() {
        if (recording != null) return
        useFrontCamera = !useFrontCamera
        controller.isReady = false
        bind()
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun applyManualExposure() {
        val activeCamera = camera ?: return
        if (controller.capabilities.canControlIso) {
            val exposureNanos = (10_000_000.0 * 2.0.pow(controller.brightness.toDouble()))
                .toLong().coerceIn(1_000_000L, 100_000_000L)
            val options = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, controller.iso.roundToInt())
                .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureNanos)
                .build()
            Camera2CameraControl.from(activeCamera.cameraControl).captureRequestOptions = options
        } else {
            val state = activeCamera.cameraInfo.exposureState
            if (state.isExposureCompensationSupported) {
                val step = state.exposureCompensationStep.toFloat().takeIf { it > 0f } ?: return
                val index = (controller.brightness / step).roundToInt()
                    .coerceIn(state.exposureCompensationRange.lower, state.exposureCompensationRange.upper)
                activeCamera.cameraControl.setExposureCompensationIndex(index)
            }
        }
    }

    fun takePhoto() {
        val capture = imageCapture ?: return
        val name = timestamp()
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "CAM_$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ConsoleApp")
            }
        }
        val options = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values,
        ).build()
        capture.takePicture(options, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                controller.statusMessage = "照片已儲存至相簿"
            }

            override fun onError(exception: ImageCaptureException) {
                controller.statusMessage = "拍照失敗：${exception.message ?: "無法儲存照片"}"
            }
        })
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun toggleRecording() {
        val active = recording
        if (active != null) {
            active.stop()
            return
        }
        val capture = videoCapture ?: return
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "VID_${timestamp()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ConsoleApp")
            }
        }
        val output = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        ).setContentValues(values).build()
        var pending = capture.output.prepareRecording(context, output)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pending = pending.withAudioEnabled()
        }
        recording = pending.start(executor) { event ->
            when (event) {
                is VideoRecordEvent.Start -> controller.isRecording = true
                is VideoRecordEvent.Finalize -> {
                    recording = null
                    controller.isRecording = false
                    controller.statusMessage = if (event.hasError()) {
                        "錄影失敗：${event.cause?.message ?: "錯誤代碼 ${event.error}"}"
                    } else "影片已儲存至相簿"
                }
            }
        }
    }

    fun close() {
        recording?.stop()
        recording = null
        provider?.unbindAll()
        controller.isReady = false
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun timestamp() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
}

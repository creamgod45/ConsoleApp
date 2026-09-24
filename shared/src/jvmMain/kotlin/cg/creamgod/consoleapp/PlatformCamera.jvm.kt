package cg.creamgod.consoleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jcodec.api.awt.AWTSequenceEncoder
import java.awt.Color as AwtColor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.pow

@Composable
actual fun rememberPlatformCameraController(): PlatformCameraController {
    val controller = remember {
        PlatformCameraController(
            CameraCapabilities(
                platformName = "Desktop/JVM",
                hasCamera = true,
                canRecordVideo = true,
                canSwitchCamera = true,
                canControlIso = true,
                canControlBrightness = true,
                rendersEffects = true,
                isoRange = 100f..1600f,
                brightnessRange = -2f..2f,
            ),
        )
    }
    val scope = rememberCoroutineScope()
    val session = remember(controller) { DesktopCameraSession(controller) }
    controller.platformHandle = session

    DisposableEffect(session) {
        scope.launch(Dispatchers.IO) {
            val result = session.start()
            withContext(Dispatchers.Main) {
                controller.isReady = result
                if (!result) {
                    controller.capabilities = controller.capabilities.copy(hasCamera = false)
                    controller.statusMessage = "找不到可用的 Webcam，或裝置正被其他程式使用"
                }
            }
        }
        onDispose { session.close() }
    }

    controller.onSwitchCamera = {
        scope.launch(Dispatchers.IO) {
            val switched = session.switchCamera()
            withContext(Dispatchers.Main) {
                controller.isReady = switched
                controller.statusMessage = if (switched) "已切換 Webcam" else "無法切換 Webcam"
            }
        }
    }
    controller.onTakePhoto = { session.takePhoto() }
    controller.onToggleRecording = { session.toggleRecording() }
    return controller
}

@Composable
actual fun PlatformCameraPreview(controller: PlatformCameraController, modifier: Modifier) {
    val session = controller.platformHandle as? DesktopCameraSession ?: return
    SwingPanel(
        factory = { DesktopPreviewPanel(session) },
        modifier = modifier,
        background = Color.Black,
    )
}

private class DesktopPreviewPanel(private val session: DesktopCameraSession) : JPanel() {
    init {
        background = AwtColor.BLACK
        session.attach(this)
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val image = session.latestFrame ?: return
        val scale = minOf(width.toDouble() / image.width, height.toDouble() / image.height)
        val drawWidth = (image.width * scale).toInt()
        val drawHeight = (image.height * scale).toInt()
        graphics.drawImage(image, (width - drawWidth) / 2, (height - drawHeight) / 2, drawWidth, drawHeight, null)
    }
}

private class DesktopCameraSession(private val controller: PlatformCameraController) {
    private val executor = Executors.newSingleThreadScheduledExecutor { task ->
        Thread(task, "desktop-camera").apply { isDaemon = true }
    }
    private var cameraIndex = 0
    private var webcam: Webcam? = null
    private var pollTask: ScheduledFuture<*>? = null
    private var panel: JPanel? = null
    private var encoder: AWTSequenceEncoder? = null
    private var recordingFile: File? = null

    @Volatile
    var latestFrame: BufferedImage? = null
        private set

    fun attach(value: JPanel) { panel = value }

    fun start(): Boolean {
        return runCatching {
            val cameras = Webcam.getWebcams()
            if (cameras.isEmpty()) return false
            controller.capabilities = controller.capabilities.copy(canSwitchCamera = cameras.size > 1)
            val selected = cameras[cameraIndex.coerceIn(cameras.indices)]
            val preferred = selected.viewSizes
                .filter { it.width <= 1280 && it.height <= 720 }
                .maxByOrNull { it.width * it.height }
                ?: Dimension(640, 480)
            selected.viewSize = preferred
            selected.open()
            webcam = selected
            pollTask?.cancel(false)
            pollTask = executor.scheduleAtFixedRate(::readFrame, 0, 66, TimeUnit.MILLISECONDS)
            true
        }.getOrElse { false }
    }

    private fun readFrame() {
        val source = runCatching { webcam?.image }.getOrNull() ?: return
        val processed = applyEffects(source)
        latestFrame = processed
        encoder?.let { current ->
            runCatching { current.encodeImage(processed) }
                .onFailure { stopRecording("錄影失敗：${it.message ?: "無法編碼影像"}") }
        }
        panel?.let { SwingUtilities.invokeLater(it::repaint) }
    }

    fun switchCamera(): Boolean {
        val cameras = Webcam.getWebcams()
        if (cameras.size < 2) return false
        stopRecording()
        pollTask?.cancel(false)
        webcam?.close()
        cameraIndex = (cameraIndex + 1) % cameras.size
        return start()
    }

    fun takePhoto() {
        executor.execute {
            val image = latestFrame
            if (image == null) {
                controller.statusMessage = "相機尚未準備完成"
                return@execute
            }
            runCatching {
                val file = outputFile("Pictures", "jpg")
                ImageIO.write(image, "jpg", file)
                file
            }.onSuccess {
                controller.statusMessage = "照片已儲存：${it.absolutePath}"
            }.onFailure {
                controller.statusMessage = "拍照失敗：${it.message ?: "無法寫入檔案"}"
            }
        }
    }

    fun toggleRecording() {
        executor.execute {
            if (encoder != null) stopRecording() else startRecording()
        }
    }

    private fun startRecording() {
        runCatching {
            val file = outputFile("Videos", "mp4")
            encoder = AWTSequenceEncoder.createSequenceEncoder(file, 15)
            recordingFile = file
            controller.isRecording = true
        }.onFailure {
            controller.statusMessage = "無法開始錄影：${it.message ?: "編碼器初始化失敗"}"
        }
    }

    private fun stopRecording(error: String? = null) {
        val active = encoder ?: return
        encoder = null
        runCatching { active.finish() }
        controller.isRecording = false
        controller.statusMessage = error ?: "影片已儲存：${recordingFile?.absolutePath}"
        recordingFile = null
    }

    fun close() {
        executor.execute {
            stopRecording()
            pollTask?.cancel(false)
            webcam?.close()
            latestFrame = null
        }
        executor.shutdown()
    }

    private fun applyEffects(source: BufferedImage): BufferedImage {
        val output = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_RGB)
        val exposure = (controller.iso / 100f).toDouble().pow(.35).toFloat() *
            2.0.pow(controller.brightness.toDouble()).toFloat()
        val saturation = controller.chroma
        val hsb = FloatArray(3)
        for (y in 0 until source.height) for (x in 0 until source.width) {
            val rgb = source.getRGB(x, y)
            var red = ((rgb shr 16) and 0xff) * exposure
            var green = ((rgb shr 8) and 0xff) * exposure
            var blue = (rgb and 0xff) * exposure
            when (controller.filter) {
                CameraFilter.WARM -> { red *= 1.1f; blue *= .88f }
                CameraFilter.COOL -> { red *= .88f; blue *= 1.1f }
                CameraFilter.VIVID -> { red *= 1.04f; green *= 1.04f; blue *= 1.04f }
                else -> Unit
            }
            AwtColor.RGBtoHSB(red.toInt().coerceIn(0, 255), green.toInt().coerceIn(0, 255), blue.toInt().coerceIn(0, 255), hsb)
            val adjustedSaturation = if (controller.filter == CameraFilter.MONO) 0f
            else (hsb[1] * saturation * if (controller.filter == CameraFilter.VIVID) 1.25f else 1f).coerceIn(0f, 1f)
            output.setRGB(x, y, AwtColor.HSBtoRGB(hsb[0], adjustedSaturation, hsb[2]))
        }
        return output
    }

    private fun outputFile(folder: String, extension: String): File {
        val root = File(System.getProperty("user.home"), "$folder/ConsoleApp")
        check(root.exists() || root.mkdirs()) { "無法建立 ${root.absolutePath}" }
        val name = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return File(root, "CAM_$name.$extension")
    }
}

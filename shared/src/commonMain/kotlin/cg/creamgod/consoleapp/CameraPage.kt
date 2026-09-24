package cg.creamgod.consoleapp

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class CameraFilter(val label: String) {
    NONE("原色"), MONO("黑白"), WARM("暖色"), COOL("冷色"), VIVID("鮮豔")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(onPageSelected: (Page) -> Unit) {
    val controller = rememberPlatformCameraController()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(controller.statusMessage) {
        controller.statusMessage?.let {
            snackbar.showSnackbar(it)
            controller.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("相機") },
                navigationIcon = {
                    IconButton(onClick = { onPageSelected(Page.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (controller.capabilities.canSwitchCamera) {
                        IconButton(onClick = controller::switchCamera) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "切換鏡頭")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CameraViewport(controller)
            if (!controller.capabilities.hasCamera) {
                UnsupportedCameraCard(controller.capabilities.platformName)
            } else {
                CameraControls(controller)
            }
        }
    }
}

@Composable
private fun CameraViewport(controller: PlatformCameraController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        PlatformCameraPreview(controller, Modifier.fillMaxSize())
        if (!controller.capabilities.rendersEffects) {
            FilterOverlay(controller.filter, controller.chroma)
        }

        if (!controller.isReady) {
            Text(
                text = when {
                    !controller.capabilities.hasCamera -> "此平台沒有可用的相機後端"
                    controller.permissionDenied -> "需要相機權限"
                    else -> "正在啟動相機…"
                },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (controller.isRecording) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = .55f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(9.dp).background(Color.Red, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("錄影中", color = Color.White)
            }
        }

        if (controller.permissionDenied) {
            Button(
                onClick = controller::requestPermission,
                modifier = Modifier.align(Alignment.Center),
            ) { Text("授權相機與麥克風") }
        }
    }
}

@Composable
private fun FilterOverlay(filter: CameraFilter, chroma: Float) {
    val overlay = when (filter) {
        CameraFilter.NONE -> Color.Transparent
        CameraFilter.MONO -> Color.Gray.copy(alpha = .18f)
        CameraFilter.WARM -> Color(0xFFFF8A45).copy(alpha = .12f + chroma * .08f)
        CameraFilter.COOL -> Color(0xFF3D8BFF).copy(alpha = .12f + chroma * .08f)
        CameraFilter.VIVID -> Color(0xFFFF2D91).copy(alpha = .08f + chroma * .06f)
    }
    if (overlay != Color.Transparent) Box(Modifier.fillMaxSize().background(overlay))
}

@Composable
private fun CameraControls(controller: PlatformCameraController) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CameraSlider(
                label = "ISO",
                valueLabel = controller.iso.roundToInt().toString(),
                value = controller.iso,
                range = controller.capabilities.isoRange,
                enabled = controller.capabilities.canControlIso && controller.isReady,
                onValueChange = controller::setIso,
            )
            CameraSlider(
                label = "亮度",
                valueLabel = controller.brightness.asEvLabel(),
                value = controller.brightness,
                range = controller.capabilities.brightnessRange,
                enabled = controller.capabilities.canControlBrightness && controller.isReady,
                onValueChange = controller::setBrightness,
            )
            CameraSlider(
                label = "色度",
                valueLabel = "${(controller.chroma * 100).roundToInt()}%",
                value = controller.chroma,
                range = 0f..2f,
                enabled = controller.isReady,
                onValueChange = controller::setChroma,
            )

            Text("濾鏡", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CameraFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = controller.filter == filter,
                        onClick = { controller.setFilter(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CaptureButton(
                    label = "拍照",
                    enabled = controller.isReady && !controller.isRecording,
                    onClick = controller::takePhoto,
                ) { Icon(Icons.Default.CameraAlt, contentDescription = null) }
                CaptureButton(
                    label = if (controller.isRecording) "停止" else "錄影",
                    enabled = controller.isReady && controller.capabilities.canRecordVideo,
                    onClick = controller::toggleRecording,
                ) {
                    Icon(
                        if (controller.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (controller.isRecording) MaterialTheme.colorScheme.error else Color.Red,
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(valueLabel, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
        )
    }
}

@Composable
private fun CaptureButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) { Box(contentAlignment = Alignment.Center) { icon() } }
        Spacer(Modifier.height(5.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun UnsupportedCameraCard(platform: String) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("此平台目前無法直接使用相機", style = MaterialTheme.typography.titleMedium)
            Text("$platform 未提供可用的原生相機後端；頁面已透過平台介面安全降級。")
        }
    }
}

private fun Float.asEvLabel(): String {
    val rounded = (this * 10).roundToInt() / 10f
    return if (rounded >= 0f) "+$rounded EV" else "$rounded EV"
}

package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.feedback.DsCallout
import cg.creamgod.consoleapp.designsystem.components.feedback.DsToastHost
import cg.creamgod.consoleapp.designsystem.components.feedback.DsToastState
import cg.creamgod.consoleapp.designsystem.components.feedback.rememberDsToastState

typealias ToastState = DsToastState

@Composable
fun Alert(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "提示",
    tone: Tone = Tone.Info,
    dismissible: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    DsCallout(
        title = title,
        message = message,
        modifier = modifier,
        tone = tone,
        action = if (dismissible) {
            { CloseButton(onClick = onDismiss) }
        } else {
            null
        },
    )
}

@Composable
fun Callout(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    tone: Tone = Tone.Info,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = DsCallout(title, modifier, message, tone, icon, action, content)

@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    pill: Boolean = false,
) {
    val colors = toneColors(tone)
    Surface(
        modifier = modifier,
        color = colors.color,
        contentColor = colors.onColor,
        shape = if (pill) androidx.compose.foundation.shape.CircleShape else MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun Progress(
    value: Float,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
) {
    val colors = toneColors(tone)
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth(),
        color = colors.color,
        trackColor = colors.container,
    )
}

@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = toneColors(tone).color,
    )
}

@Composable
fun Placeholder(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "placeholder")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (animated) 0.75f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "placeholderAlpha",
    )
    Box(
        modifier = modifier
            .height(16.dp)
            .alpha(alpha),
    ) {
        Surface(
            modifier = Modifier.matchParentSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            content = {},
        )
    }
}

@Composable
fun Toast(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
) {
    if (!visible) return

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                title?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            onDismiss?.let { CloseButton(onClick = it) }
        }
    }
}

@Composable
fun rememberToastState(): ToastState = rememberDsToastState()

@Composable
fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
) = DsToastHost(state, modifier)

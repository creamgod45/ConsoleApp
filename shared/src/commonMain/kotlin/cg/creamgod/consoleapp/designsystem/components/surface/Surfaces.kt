package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

data class ListItem<T>(
    val value: T,
    val headline: String,
    val supportingText: String? = null,
    val enabled: Boolean = true,
)

data class CarouselItem(
    val label: String? = null,
    val content: @Composable () -> Unit,
)

@Composable
fun Card(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick)

    Surface(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
    ) {
        Column {
            header?.let {
                Box(modifier = Modifier.fillMaxWidth()) { it() }
                HorizontalDivider()
            }
            if (title != null || subtitle != null) {
                Column(
                    modifier = Modifier.padding(BootstrapTokens.Spacing.three),
                    verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.one),
                ) {
                    title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    subtitle?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(BootstrapTokens.Spacing.three),
                content = content,
            )
            footer?.let {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BootstrapTokens.Spacing.three),
                    horizontalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.two),
                    content = it,
                )
            }
        }
    }
}

@Composable
fun <T> ListGroup(
    items: List<ListItem<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    selected: T? = null,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                val active = item.value == selected
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) { onSelect(item.value) },
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                ) {
                    Column(modifier = Modifier.padding(BootstrapTokens.Spacing.three)) {
                        Text(item.headline, style = MaterialTheme.typography.bodyLarge)
                        item.supportingText?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                if (index != items.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
fun Carousel(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    showControls: Boolean = true,
    showIndicators: Boolean = true,
) {
    if (items.isEmpty()) return
    var index by remember(items) { mutableIntStateOf(initialIndex.coerceIn(items.indices)) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            items[index].content()
            if (showControls && items.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { index = if (index == 0) items.lastIndex else index - 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一個")
                    }
                    IconButton(onClick = { index = if (index == items.lastIndex) 0 else index + 1 }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下一個")
                    }
                }
            }
        }
        items[index].label?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        if (showIndicators && items.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items.indices.forEach { itemIndex ->
                    RadioButton(
                        selected = itemIndex == index,
                        onClick = { index = itemIndex },
                    )
                }
            }
        }
    }
}

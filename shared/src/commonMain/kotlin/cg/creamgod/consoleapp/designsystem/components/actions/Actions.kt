package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton as MaterialExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton as MaterialFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class ButtonItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

data class DropdownItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

data class FabMenuItem<T>(
    val value: T,
    val label: String,
    val icon: @Composable () -> Unit,
    val enabled: Boolean = true,
)

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    variant: Variant = Variant.Filled,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = toneColors(tone)
    val content: @Composable RowScope.() -> Unit = {
        leadingIcon?.invoke()
        Text(text, modifier = Modifier.padding(horizontal = 4.dp))
        trailingIcon?.invoke()
    }

    when (variant) {
        Variant.Filled -> MaterialButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.color,
                contentColor = colors.onColor,
            ),
            content = content,
        )

        Variant.Outline -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            border = BorderStroke(1.dp, colors.color),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.color),
            content = content,
        )

        Variant.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.color),
            content = content,
        )
    }
}

@Composable
fun <T> ButtonGroup(
    items: List<ButtonItem<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEach { item ->
            Button(
                text = item.label,
                onClick = { onSelect(item.value) },
                enabled = item.enabled,
                tone = tone,
                variant = if (item.value == selected) Variant.Filled else Variant.Outline,
            )
        }
    }
}

@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String = "關閉",
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled, contentDescription = description) {
        Icon(Icons.Default.Close, contentDescription = description)
    }
}

/** A design-system icon button with an explicit accessibility description. */
@Composable
fun IconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialIconButton(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        enabled = enabled,
    ) {
        content()
    }
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
) {
    val colors = toneColors(tone)
    MaterialFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = colors.container,
        contentColor = colors.onContainer,
        content = icon,
    )
}

@Composable
fun ExtendedFloatingActionButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    tone: Tone = Tone.Primary,
) {
    val colors = toneColors(tone)
    MaterialExtendedFloatingActionButton(
        text = { Text(text) },
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        containerColor = colors.container,
        contentColor = colors.onContainer,
    )
}

/**
 * Controlled FAB menu. [expanded] belongs to the caller so closing it can also be
 * coordinated with navigation, validation, or a ViewModel.
 */
@Composable
fun <T> FabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<FabMenuItem<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    mainIcon: @Composable (expanded: Boolean) -> Unit = { isExpanded ->
        Icon(
            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
            contentDescription = if (isExpanded) "關閉動作選單" else "開啟動作選單",
        )
    },
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (expanded) {
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.label)
                    SmallFloatingActionButton(
                        onClick = {
                            if (item.enabled) {
                                onSelect(item.value)
                                onExpandedChange(false)
                            }
                        },
                        modifier = Modifier
                            .alpha(if (item.enabled) 1f else 0.38f)
                            .semantics { if (!item.enabled) disabled() },
                        containerColor = toneColors(Tone.Secondary).container,
                        contentColor = toneColors(Tone.Secondary).onContainer,
                    ) {
                        item.icon()
                    }
                }
            }
        }
        MaterialFloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            elevation = FloatingActionButtonDefaults.elevation(),
        ) {
            mainIcon(expanded)
        }
    }
}

/** A single-selection, controlled segmented button group. */
@Composable
fun <T> SegmentedButtons(
    items: List<ButtonItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
) {
    Row(modifier = modifier) {
        items.forEach { item ->
            Button(
                text = item.label,
                onClick = { onSelect(item.value) },
                enabled = item.enabled,
                tone = tone,
                variant = if (item.value == selected) Variant.Filled else Variant.Outline,
            )
        }
    }
}

/** A primary action plus a menu of closely related alternative actions. */
@Composable
fun <T> SplitButton(
    text: String,
    onClick: () -> Unit,
    items: List<DropdownItem<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        Row {
            Button(text = text, onClick = onClick, enabled = enabled)
            MaterialButton(
                onClick = { expanded = true },
                enabled = enabled,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "更多動作")
            }
        }
        MaterialDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    enabled = item.enabled,
                    onClick = {
                        onSelect(item.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun <T> Dropdown(
    label: String,
    items: List<DropdownItem<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        Button(
            text = label,
            onClick = { expanded = true },
            enabled = enabled,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "展開")
            },
        )
        MaterialDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    enabled = item.enabled,
                    onClick = {
                        onSelect(item.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

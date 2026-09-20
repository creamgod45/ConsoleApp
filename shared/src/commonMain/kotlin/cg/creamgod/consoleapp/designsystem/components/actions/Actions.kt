package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(Icons.Default.Close, contentDescription = description)
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

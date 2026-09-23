package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.material3.AssistChip as MaterialAssistChip
import androidx.compose.material3.FilterChip as MaterialFilterChip
import androidx.compose.material3.InputChip as MaterialInputChip
import androidx.compose.material3.SuggestionChip as MaterialSuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    MaterialAssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    MaterialFilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

@Composable
fun InputChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    avatar: (@Composable () -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    MaterialInputChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        avatar = avatar,
        trailingIcon = onRemove?.let { remove ->
            { CloseButton(onClick = remove, description = "移除 $label") }
        },
    )
}

@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    MaterialSuggestionChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        icon = icon,
    )
}

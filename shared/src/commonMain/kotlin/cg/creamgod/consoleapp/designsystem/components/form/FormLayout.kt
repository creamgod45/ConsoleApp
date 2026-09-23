package cg.creamgod.consoleapp.designsystem.components.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.AssistChip
import cg.creamgod.consoleapp.designsystem.tokens.BootstrapTokens

@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.three),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(BootstrapTokens.Spacing.one)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            description?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        content()
    }
}

@Composable
fun FormInputGroup(
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        prefix?.let { InputAddon(it) }
        content()
        suffix?.let { InputAddon(it) }
    }
}

@Composable
fun FormFilePicker(
    fileNames: List<String>,
    onPick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    allowMultiple: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onPick, enabled = enabled) {
                Text(if (allowMultiple) "選擇多個檔案" else "選擇檔案")
            }
            Text(
                text = fileNames.joinToString().ifBlank { "尚未選擇檔案" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (fileNames.isNotEmpty() && onClear != null) {
                OutlinedButton(onClick = onClear, enabled = enabled) {
                    Text("清除")
                }
            }
        }
        FormSupportingText(helperText = helperText, errorText = errorText)
    }
}

@Composable
fun FormActions(
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    submitText: String = "送出",
    submitEnabled: Boolean = true,
    onReset: (() -> Unit)? = null,
    resetText: String = "重設",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        onReset?.let {
            OutlinedButton(onClick = it) { Text(resetText) }
        }
        Button(onClick = onSubmit, enabled = submitEnabled) { Text(submitText) }
    }
}

@Composable
private fun InputAddon(text: String) {
    Surface(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp))
    }
}


@Composable
fun <T> DropdownChip(
    value: T?,
    options: List<FormOption<T>>,
    onValueChange: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: String = "請選擇",
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(horizontal = 4.dp)) {
        AssistChip(
            label = value?.let(label) ?: placeholder,
            onClick = { expanded = true },
            leadingIcon = leadingIcon ?: {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    enabled = option.enabled,
                    leadingIcon = if (option.value == value) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onValueChange(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

package cg.creamgod.consoleapp.designsystem.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FormOption<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

@Composable
fun <T> FormSelect(
    value: T?,
    options: List<FormOption<T>>,
    onValueChange: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "請選擇",
    helperText: String? = null,
    errorText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == value }?.label ?: placeholder

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Text(selectedLabel, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "展開選項")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        enabled = option.enabled,
                        onClick = {
                            onValueChange(option.value)
                            expanded = false
                        },
                    )
                }
            }
        }
        FormSupportingText(helperText = helperText, errorText = errorText)
    }
}

@Composable
fun <T> FormMultiSelect(
    values: Set<T>,
    options: List<FormOption<T>>,
    onValuesChange: (Set<T>) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "請選擇",
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabels = options.filter { it.value in values }.joinToString { it.label }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Text(selectedLabels.ifBlank { placeholder }, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "展開多選選項")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    val selected = option.value in values
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        leadingIcon = { Checkbox(checked = selected, onCheckedChange = null) },
                        enabled = option.enabled,
                        onClick = {
                            onValuesChange(
                                if (selected) values - option.value else values + option.value,
                            )
                        },
                    )
                }
            }
        }
        FormSupportingText(helperText = helperText, errorText = errorText)
    }
}

@Composable
fun FormCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onCheckedChange(!checked) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
            Text(label)
        }
        FormSupportingText(helperText = helperText, modifier = Modifier.padding(start = 48.dp))
    }
}

@Composable
fun <T> FormRadioGroup(
    value: T?,
    options: List<FormOption<T>>,
    onValueChange: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled && option.enabled) { onValueChange(option.value) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = value == option.value,
                    onClick = { onValueChange(option.value) },
                    enabled = enabled && option.enabled,
                )
                Text(option.label)
            }
        }
        FormSupportingText(helperText = helperText, errorText = errorText)
    }
}

@Composable
fun FormSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label)
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
        FormSupportingText(helperText = helperText)
    }
}

@Composable
fun FormRange(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    valueText: (Float) -> String = { it.toString() },
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(valueText(value), style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
        )
    }
}

@Composable
internal fun FormSupportingText(
    helperText: String? = null,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    val message = errorText ?: helperText ?: return
    Text(
        text = message,
        modifier = modifier,
        color = if (errorText == null) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.error
        },
        style = MaterialTheme.typography.bodySmall,
    )
}

package cg.creamgod.consoleapp.designsystem.components.overlay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import cg.creamgod.consoleapp.designsystem.components.form.FormOption

@Composable
fun DsConfirmDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "確認",
    dismissText: String = "取消",
    destructive: Boolean = false,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            if (destructive) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(confirmText)
                }
            } else {
                Button(onClick = onConfirm) { Text(confirmText) }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(dismissText) }
        },
    )
}

@Composable
fun DsPromptDialog(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    label: String,
    placeholder: String? = null,
    errorText: String? = null,
    confirmText: String = "確認",
    dismissText: String = "取消",
    confirmEnabled: Boolean = errorText == null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column {
                message?.let { Text(it) }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    placeholder = placeholder?.let { { Text(it) } },
                    supportingText = errorText?.let { { Text(it) } },
                    isError = errorText != null,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = confirmEnabled,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(dismissText) }
        },
    )
}

@Composable
fun DsAskDialog(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
    title: String,
    label: String,
    message: String? = null,
    placeholder: String? = null,
    errorText: String? = null,
    confirmText: String = "確認",
    dismissText: String = "取消",
    confirmEnabled: Boolean = errorText == null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) = DsPromptDialog(
    visible = visible,
    value = value,
    onValueChange = onValueChange,
    onConfirm = onConfirm,
    onDismissRequest = onDismissRequest,
    title = title,
    message = message,
    label = label,
    placeholder = placeholder,
    errorText = errorText,
    confirmText = confirmText,
    dismissText = dismissText,
    confirmEnabled = confirmEnabled,
    visualTransformation = visualTransformation,
)

@Composable
fun <T> DsChoiceDialog(
    visible: Boolean,
    value: T?,
    options: List<FormOption<T>>,
    onValueChange: (T) -> Unit,
    onConfirm: (T) -> Unit,
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    confirmText: String = "選擇",
    dismissText: String = "取消",
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column {
                message?.let { Text(it) }
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = option.enabled) { onValueChange(option.value) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == option.value,
                            onClick = { onValueChange(option.value) },
                            enabled = option.enabled,
                        )
                        Text(option.label)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { value?.let(onConfirm) },
                enabled = value != null,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(dismissText) }
        },
    )
}

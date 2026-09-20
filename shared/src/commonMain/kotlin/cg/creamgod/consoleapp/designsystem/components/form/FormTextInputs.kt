package cg.creamgod.consoleapp.designsystem.components.form

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

enum class FormInputType {
    Text,
    Email,
    Password,
    Number,
    Decimal,
    Phone,
    Url,
    Search,
    Date,
    Time,
}

@Composable
fun FormTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    type: FormInputType = FormInputType.Text,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val support = errorText ?: helperText

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = errorText != null,
        label = { Text(if (required) "$label *" else label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = support?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = type.keyboardType()),
        keyboardActions = keyboardActions,
    )
}

@Composable
fun FormPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    FormTextInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        type = FormInputType.Password,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        required = required,
        enabled = enabled,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "隱藏密碼" else "顯示密碼",
                )
            }
        },
    )
}

@Composable
fun FormTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 8,
) {
    FormTextInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        required = required,
        enabled = enabled,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
    )
}

private fun FormInputType.keyboardType(): KeyboardType = when (this) {
    FormInputType.Email -> KeyboardType.Email
    FormInputType.Password -> KeyboardType.Password
    FormInputType.Number -> KeyboardType.Number
    FormInputType.Decimal -> KeyboardType.Decimal
    FormInputType.Phone -> KeyboardType.Phone
    FormInputType.Url -> KeyboardType.Uri
    FormInputType.Text,
    FormInputType.Search,
    FormInputType.Date,
    FormInputType.Time,
    -> KeyboardType.Text
}

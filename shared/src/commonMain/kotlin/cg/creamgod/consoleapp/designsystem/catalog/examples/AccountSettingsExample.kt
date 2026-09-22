package cg.creamgod.consoleapp.designsystem.catalog.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cg.creamgod.consoleapp.designsystem.components.Alert
import cg.creamgod.consoleapp.designsystem.components.Confirm
import cg.creamgod.consoleapp.designsystem.components.Tone
import cg.creamgod.consoleapp.designsystem.components.form.FormActions
import cg.creamgod.consoleapp.designsystem.components.form.FormInputType
import cg.creamgod.consoleapp.designsystem.components.form.FormOption
import cg.creamgod.consoleapp.designsystem.components.form.FormRange
import cg.creamgod.consoleapp.designsystem.components.form.FormSection
import cg.creamgod.consoleapp.designsystem.components.form.FormSelect
import cg.creamgod.consoleapp.designsystem.components.form.FormSwitch
import cg.creamgod.consoleapp.designsystem.components.form.FormTextInput
import cg.creamgod.consoleapp.designsystem.components.form.FormValidators
import kotlinx.coroutines.launch

enum class AccountPlan(val label: String) {
    Free("Free"),
    Pro("Pro"),
    Team("Team"),
}

data class AccountSettings(
    val displayName: String,
    val email: String,
    val plan: AccountPlan,
    val productUpdates: Boolean,
    val warningThreshold: Float,
)

data class AccountSettingsValidation(
    val displayNameError: String? = null,
    val emailError: String? = null,
) {
    val isValid: Boolean get() = displayNameError == null && emailError == null
}

fun validateAccountSettings(value: AccountSettings): AccountSettingsValidation {
    val name = FormValidators.all(
        FormValidators.required("Display name is required"),
        FormValidators.minLength(2, "Use at least 2 characters"),
        FormValidators.maxLength(40, "Use at most 40 characters"),
    ).validate(value.displayName)
    val email = FormValidators.all(
        FormValidators.required("Email is required"),
        FormValidators.email("Enter a valid email address"),
    ).validate(value.email)

    return AccountSettingsValidation(
        displayNameError = name.message,
        emailError = email.message,
    )
}

/**
 * A complete, reusable settings form. The caller owns persistence through [onSave];
 * this helper owns only editable draft state and transient presentation state.
 */
@Composable
fun AccountSettingsForm(
    initialValue: AccountSettings,
    onSave: suspend (AccountSettings) -> Result<Unit>,
    modifier: Modifier = Modifier,
) {
    var baseline by remember(initialValue) { mutableStateOf(initialValue) }
    var draft by remember(initialValue) { mutableStateOf(initialValue) }
    var saving by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var resetConfirmationVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val validation = remember(draft) { validateAccountSettings(draft) }
    fun updateDraft(transform: (AccountSettings) -> AccountSettings) {
        draft = transform(draft)
        successMessage = null
        saveError = null
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        successMessage?.let { message ->
            Alert(
                message = message,
                tone = Tone.Success,
                dismissible = true,
                onDismiss = { successMessage = null },
            )
        }
        saveError?.let { message ->
            Alert(
                title = "Unable to save settings",
                message = message,
                tone = Tone.Danger,
                dismissible = true,
                onDismiss = { saveError = null },
            )
        }

        FormSection(
            title = "Account settings",
            description = "Profile, plan, and notification preferences.",
        ) {
            FormTextInput(
                value = draft.displayName,
                onValueChange = { value -> updateDraft { it.copy(displayName = value) } },
                label = "Display name",
                required = true,
                errorText = validation.displayNameError,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            )
            FormTextInput(
                value = draft.email,
                onValueChange = { value -> updateDraft { it.copy(email = value) } },
                label = "Email",
                type = FormInputType.Email,
                required = true,
                errorText = validation.emailError,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            )
            FormSelect(
                value = draft.plan,
                options = AccountPlan.entries.map { FormOption(it, it.label) },
                onValueChange = { value -> updateDraft { it.copy(plan = value) } },
                label = "Plan",
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            )
            FormSwitch(
                checked = draft.productUpdates,
                onCheckedChange = { value -> updateDraft { it.copy(productUpdates = value) } },
                label = "Product updates",
                helperText = "Receive release and maintenance announcements.",
                enabled = !saving,
            )
            FormRange(
                value = draft.warningThreshold,
                onValueChange = { value -> updateDraft { it.copy(warningThreshold = value) } },
                label = "Usage warning",
                valueRange = 50f..100f,
                steps = 9,
                enabled = !saving,
                valueText = { "${it.toInt()}%" },
            )
            FormActions(
                onSubmit = {
                    scope.launch {
                        saving = true
                        successMessage = null
                        saveError = null
                        val result = runCatching { onSave(draft).getOrThrow() }
                        saving = false
                        result.onSuccess {
                            baseline = draft
                            successMessage = "Account settings saved"
                        }.onFailure { error ->
                            saveError = error.message ?: "An unexpected error occurred"
                        }
                    }
                },
                submitText = if (saving) "Saving…" else "Save settings",
                submitEnabled = validation.isValid && !saving,
                onReset = if (saving) null else ({ resetConfirmationVisible = true }),
                resetText = "Reset",
            )
        }
    }

    Confirm(
        visible = resetConfirmationVisible,
        title = "Reset changes?",
        message = "All unsaved changes will be replaced with the original values.",
        confirmText = "Reset changes",
        onConfirm = {
            draft = baseline
            successMessage = null
            saveError = null
            resetConfirmationVisible = false
        },
        onDismiss = { resetConfirmationVisible = false },
    )
}

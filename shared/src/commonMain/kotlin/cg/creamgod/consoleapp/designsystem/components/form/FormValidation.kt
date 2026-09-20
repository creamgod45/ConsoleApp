package cg.creamgod.consoleapp.designsystem.components.form

data class FormValidation(
    val isValid: Boolean,
    val message: String? = null,
) {
    companion object {
        val Valid = FormValidation(isValid = true)

        fun invalid(message: String) = FormValidation(
            isValid = false,
            message = message,
        )
    }
}

fun interface StringValidator {
    fun validate(value: String): FormValidation
}

object FormValidators {
    fun required(message: String = "此欄位為必填") = StringValidator { value ->
        if (value.isBlank()) FormValidation.invalid(message) else FormValidation.Valid
    }

    fun email(message: String = "請輸入有效的 Email") = StringValidator { value ->
        val parts = value.trim().split('@')
        val isValid = parts.size == 2 &&
            parts[0].isNotBlank() &&
            parts[1].contains('.') &&
            !parts[1].startsWith('.') &&
            !parts[1].endsWith('.')

        if (value.isBlank() || isValid) FormValidation.Valid else FormValidation.invalid(message)
    }

    fun minLength(length: Int, message: String = "至少需要 $length 個字元") = StringValidator { value ->
        if (value.length >= length) FormValidation.Valid else FormValidation.invalid(message)
    }

    fun maxLength(length: Int, message: String = "最多只能輸入 $length 個字元") = StringValidator { value ->
        if (value.length <= length) FormValidation.Valid else FormValidation.invalid(message)
    }

    fun pattern(regex: Regex, message: String) = StringValidator { value ->
        if (value.isBlank() || regex.matches(value)) FormValidation.Valid else FormValidation.invalid(message)
    }

    fun all(vararg validators: StringValidator) = StringValidator { value ->
        validators
            .asSequence()
            .map { it.validate(value) }
            .firstOrNull { !it.isValid }
            ?: FormValidation.Valid
    }
}

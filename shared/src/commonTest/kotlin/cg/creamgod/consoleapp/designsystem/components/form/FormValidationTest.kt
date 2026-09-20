package cg.creamgod.consoleapp.designsystem.components.form

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormValidationTest {
    @Test
    fun requiredRejectsBlankValues() {
        assertFalse(FormValidators.required().validate("   ").isValid)
        assertTrue(FormValidators.required().validate("value").isValid)
    }

    @Test
    fun emailAllowsBlankForOptionalFieldsAndRejectsMalformedValues() {
        assertTrue(FormValidators.email().validate("").isValid)
        assertTrue(FormValidators.email().validate("hello@example.com").isValid)
        assertFalse(FormValidators.email().validate("hello@example").isValid)
    }

    @Test
    fun composedValidationReturnsTheFirstFailure() {
        val validator = FormValidators.all(
            FormValidators.required("required"),
            FormValidators.minLength(5, "too short"),
        )

        assertTrue(validator.validate("hello").isValid)
        assertFalse(validator.validate("").isValid)
        assertFalse(validator.validate("four").isValid)
    }
}

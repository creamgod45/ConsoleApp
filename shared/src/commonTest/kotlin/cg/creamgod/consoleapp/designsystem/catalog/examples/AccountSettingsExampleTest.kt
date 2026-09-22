package cg.creamgod.consoleapp.designsystem.catalog.examples

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountSettingsExampleTest {
    @Test
    fun validatesRequiredBusinessFields() {
        val invalid = validateAccountSettings(
            AccountSettings("", "invalid", AccountPlan.Free, false, 80f),
        )
        val valid = validateAccountSettings(
            AccountSettings("Ada", "ada@example.com", AccountPlan.Pro, true, 80f),
        )

        assertFalse(invalid.isValid)
        assertTrue(valid.isValid)
    }
}

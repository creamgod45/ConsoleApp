package cg.creamgod.consoleapp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MailApiTest {
    @Test
    fun parsesFakeMailResponse() {
        val result = parseMailMessages(
            """[{"id":1,"name":"Ada Lovelace","email":"ada@example.com","received_at":"2026-09-23T10:30:00","content":"Hello","attachments":[{"id":"a1","filename":"report.pdf","content_type":"application/pdf","size_bytes":12,"content":"Preview","is_inline":false}]}]""",
        )

        assertEquals(1, result.size)
        assertEquals("2026-09-23T10:30:00", result.single().receivedAt)
        assertEquals("report.pdf", result.single().attachments.single().filename)
        assertEquals("Preview", result.single().attachments.single().content)
    }

    @Test
    fun rejectsMissingRequiredFields() {
        assertFailsWith<IllegalStateException> {
            parseMailMessages("""[{"id":1,"name":"Ada"}]""")
        }
    }

    @Test
    fun acceptsMailWithoutAttachments() {
        val result = parseMailMessages(
            """[{"id":1,"name":"Ada","email":"ada@example.com","received_at":"2026-09-23T10:30:00","content":"Hello","attachments":[]}]""",
        )

        assertTrue(result.single().attachments.isEmpty())
    }
}

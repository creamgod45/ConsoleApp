package cg.creamgod.consoleapp.designsystem.content

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MarkdownParserTest {
    @Test
    fun parsesDocumentBlocks() {
        val document = MarkdownParser.parse(
            """
            # Title

            Intro with **strong**, *emphasis*, and [link](https://example.com).

            - First
            - Second

            ```kotlin
            val answer = 42
            ```

            | Name | Status |
            | --- | --- |
            | Parser | Ready |
            """.trimIndent(),
        )

        assertIs<DocumentBlock.Heading>(document.blocks[0])
        assertIs<DocumentBlock.Paragraph>(document.blocks[1])
        assertIs<DocumentBlock.BulletList>(document.blocks[2])
        assertEquals("kotlin", assertIs<DocumentBlock.Code>(document.blocks[3]).language)
        assertIs<DocumentBlock.Table>(document.blocks[4])
    }

    @Test
    fun parsesStronglyTypedInlineContent() {
        val content = MarkdownParser.parseInline("Text **bold** `code` ![alt](image.png)")

        assertEquals(6, content.size)
        assertIs<InlineContent.Strong>(content[1])
        assertIs<InlineContent.Code>(content[3])
        assertIs<InlineContent.Image>(content[5])
    }

    @Test
    fun buildsDocumentsWithTheDsl() {
        val value = document {
            h1("Quick start")
            paragraph(text("Use "), strong("semantic"), text(" content."))
            orderedList("Install", "Run")
        }

        assertEquals(3, value.blocks.size)
        assertEquals(1, assertIs<DocumentBlock.Heading>(value.blocks.first()).level)
        assertIs<DocumentBlock.OrderedList>(value.blocks.last())
    }
}

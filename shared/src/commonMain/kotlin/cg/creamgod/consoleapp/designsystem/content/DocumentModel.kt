package cg.creamgod.consoleapp.designsystem.content

/** A platform-neutral document that can be created by the DSL or a parser. */
data class Document(
    val blocks: List<DocumentBlock>,
)

sealed interface DocumentBlock {
    data class Heading(
        val level: Int,
        val content: List<InlineContent>,
    ) : DocumentBlock {
        init {
            require(level in 1..6) { "Heading level must be between 1 and 6." }
        }
    }

    data class Paragraph(val content: List<InlineContent>) : DocumentBlock
    data class Code(val source: String, val language: String? = null) : DocumentBlock
    data class Quote(val blocks: List<DocumentBlock>) : DocumentBlock
    data class BulletList(val items: List<List<DocumentBlock>>) : DocumentBlock
    data class OrderedList(val items: List<List<DocumentBlock>>, val start: Int = 1) : DocumentBlock
    data class Table(val header: List<List<InlineContent>>, val rows: List<List<List<InlineContent>>>) : DocumentBlock
    data object Divider : DocumentBlock
}

sealed interface InlineContent {
    data class Text(val value: String) : InlineContent
    data class Strong(val children: List<InlineContent>) : InlineContent
    data class Emphasis(val children: List<InlineContent>) : InlineContent
    data class Code(val value: String) : InlineContent
    data class Link(val label: List<InlineContent>, val destination: String) : InlineContent
    data class Image(val description: String, val source: String) : InlineContent
    data object LineBreak : InlineContent
}

fun text(value: String): InlineContent = InlineContent.Text(value)
fun strong(value: String): InlineContent = InlineContent.Strong(listOf(text(value)))
fun emphasis(value: String): InlineContent = InlineContent.Emphasis(listOf(text(value)))
fun inlineCode(value: String): InlineContent = InlineContent.Code(value)
fun link(label: String, destination: String): InlineContent =
    InlineContent.Link(listOf(text(label)), destination)

fun document(content: DocumentBuilder.() -> Unit): Document =
    DocumentBuilder().apply(content).build()

class DocumentBuilder internal constructor() {
    private val blocks = mutableListOf<DocumentBlock>()

    fun heading(level: Int, text: String) = add(DocumentBlock.Heading(level, listOf(InlineContent.Text(text))))
    fun heading(level: Int, vararg content: InlineContent) = add(DocumentBlock.Heading(level, content.toList()))
    fun h1(text: String) = heading(1, text)
    fun h2(text: String) = heading(2, text)
    fun h3(text: String) = heading(3, text)
    fun h4(text: String) = heading(4, text)
    fun h5(text: String) = heading(5, text)
    fun h6(text: String) = heading(6, text)
    fun paragraph(text: String) = add(DocumentBlock.Paragraph(listOf(InlineContent.Text(text))))
    fun paragraph(vararg content: InlineContent) = add(DocumentBlock.Paragraph(content.toList()))
    fun code(source: String, language: String? = null) = add(DocumentBlock.Code(source, language))
    fun divider() = add(DocumentBlock.Divider)

    fun quote(content: DocumentBuilder.() -> Unit) =
        add(DocumentBlock.Quote(DocumentBuilder().apply(content).build().blocks))

    fun bulletList(vararg items: String) = add(
        DocumentBlock.BulletList(items.map { listOf(DocumentBlock.Paragraph(listOf(InlineContent.Text(it)))) }),
    )

    fun orderedList(vararg items: String, start: Int = 1) = add(
        DocumentBlock.OrderedList(
            items = items.map { listOf(DocumentBlock.Paragraph(listOf(InlineContent.Text(it)))) },
            start = start,
        ),
    )

    fun add(block: DocumentBlock) {
        blocks += block
    }

    fun build(): Document = Document(blocks.toList())
}

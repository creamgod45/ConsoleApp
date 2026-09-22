package cg.creamgod.consoleapp.designsystem.content

/**
 * A small dependency-free Markdown parser for application documentation.
 * It supports headings, paragraphs, emphasis, links, images, fenced code,
 * quotes, ordered/unordered lists, horizontal rules, and pipe tables.
 */
object MarkdownParser {
    fun parse(markdown: String): Document {
        val lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val blocks = mutableListOf<DocumentBlock>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            if (line.isBlank()) {
                index++
                continue
            }

            val fence = fenceMarker(line)
            if (fence != null) {
                val language = line.trim().removePrefix(fence).trim().takeIf { it.isNotEmpty() }
                val code = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].trimStart().startsWith(fence)) {
                    code += lines[index++]
                }
                if (index < lines.size) index++
                blocks += DocumentBlock.Code(code.joinToString("\n"), language)
                continue
            }

            heading(line)?.let {
                blocks += it
                index++
                continue
            }

            if (isDivider(line)) {
                blocks += DocumentBlock.Divider
                index++
                continue
            }

            if (line.trimStart().startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (index < lines.size && lines[index].trimStart().startsWith(">")) {
                    quoteLines += lines[index].trimStart().removePrefix(">").removePrefix(" ")
                    index++
                }
                blocks += DocumentBlock.Quote(parse(quoteLines.joinToString("\n")).blocks)
                continue
            }

            unorderedItem(line)?.let {
                val items = mutableListOf<List<DocumentBlock>>()
                while (index < lines.size) {
                    val value = unorderedItem(lines[index]) ?: break
                    items += listOf(DocumentBlock.Paragraph(parseInline(value)))
                    index++
                }
                blocks += DocumentBlock.BulletList(items)
                continue
            }

            orderedItem(line)?.let { first ->
                val items = mutableListOf<List<DocumentBlock>>()
                while (index < lines.size) {
                    val value = orderedItem(lines[index]) ?: break
                    items += listOf(DocumentBlock.Paragraph(parseInline(value.second)))
                    index++
                }
                blocks += DocumentBlock.OrderedList(items, first.first)
                continue
            }

            if (index + 1 < lines.size && isTableSeparator(lines[index + 1]) && line.contains('|')) {
                val header = tableCells(line).map(::parseInline)
                index += 2
                val rows = mutableListOf<List<List<InlineContent>>>()
                while (index < lines.size && lines[index].isNotBlank() && lines[index].contains('|')) {
                    rows += tableCells(lines[index]).map(::parseInline)
                    index++
                }
                blocks += DocumentBlock.Table(header, rows)
                continue
            }

            val paragraph = mutableListOf(line.trim())
            index++
            while (index < lines.size && lines[index].isNotBlank() && !startsBlock(lines, index)) {
                paragraph += lines[index].trim()
                index++
            }
            blocks += DocumentBlock.Paragraph(parseInline(paragraph.joinToString("\n")))
        }

        return Document(blocks)
    }

    fun parseInline(source: String): List<InlineContent> {
        val result = mutableListOf<InlineContent>()
        var plain = StringBuilder()
        var index = 0

        fun flush() {
            if (plain.isNotEmpty()) {
                result += InlineContent.Text(plain.toString())
                plain = StringBuilder()
            }
        }

        while (index < source.length) {
            if (source[index] == '\\' && index + 1 < source.length) {
                plain.append(source[index + 1])
                index += 2
                continue
            }
            if (source[index] == '\n') {
                flush()
                result += InlineContent.LineBreak
                index++
                continue
            }

            val imageStart = source.startsWith("![", index)
            val linkStart = source.startsWith("[", index)
            if (imageStart || linkStart) {
                val labelStart = index + if (imageStart) 2 else 1
                val labelEnd = source.indexOf(']', labelStart)
                if (labelEnd >= 0 && labelEnd + 1 < source.length && source[labelEnd + 1] == '(') {
                    val destinationEnd = source.indexOf(')', labelEnd + 2)
                    if (destinationEnd >= 0) {
                        flush()
                        val label = source.substring(labelStart, labelEnd)
                        val destination = source.substring(labelEnd + 2, destinationEnd)
                        result += if (imageStart) {
                            InlineContent.Image(label, destination)
                        } else {
                            InlineContent.Link(parseInline(label), destination)
                        }
                        index = destinationEnd + 1
                        continue
                    }
                }
            }

            val delimiter = when {
                source.startsWith("**", index) -> "**"
                source.startsWith("__", index) -> "__"
                source[index] == '*' -> "*"
                source[index] == '_' -> "_"
                source[index] == '`' -> "`"
                else -> null
            }
            if (delimiter != null) {
                val end = source.indexOf(delimiter, index + delimiter.length)
                if (end >= 0) {
                    flush()
                    val value = source.substring(index + delimiter.length, end)
                    result += when (delimiter) {
                        "**", "__" -> InlineContent.Strong(parseInline(value))
                        "`" -> InlineContent.Code(value)
                        else -> InlineContent.Emphasis(parseInline(value))
                    }
                    index = end + delimiter.length
                    continue
                }
            }

            plain.append(source[index++])
        }
        flush()
        return result
    }

    private fun startsBlock(lines: List<String>, index: Int): Boolean {
        val line = lines[index]
        return fenceMarker(line) != null || heading(line) != null || isDivider(line) ||
            line.trimStart().startsWith(">") || unorderedItem(line) != null || orderedItem(line) != null ||
            (index + 1 < lines.size && line.contains('|') && isTableSeparator(lines[index + 1]))
    }

    private fun heading(line: String): DocumentBlock.Heading? {
        val trimmed = line.trimStart()
        val level = trimmed.takeWhile { it == '#' }.length
        if (level !in 1..6 || trimmed.getOrNull(level) != ' ') return null
        return DocumentBlock.Heading(level, parseInline(trimmed.drop(level + 1).trimEnd('#', ' ')))
    }

    private fun fenceMarker(line: String): String? = when {
        line.trimStart().startsWith("```") -> "```"
        line.trimStart().startsWith("~~~") -> "~~~"
        else -> null
    }

    private fun unorderedItem(line: String): String? {
        val trimmed = line.trimStart()
        return if (trimmed.length >= 2 && trimmed[0] in "-*+" && trimmed[1] == ' ') trimmed.drop(2) else null
    }

    private fun orderedItem(line: String): Pair<Int, String>? {
        val trimmed = line.trimStart()
        val digits = trimmed.takeWhile(Char::isDigit)
        if (digits.isEmpty() || trimmed.getOrNull(digits.length) != '.' || trimmed.getOrNull(digits.length + 1) != ' ') return null
        return digits.toInt() to trimmed.drop(digits.length + 2)
    }

    private fun isDivider(line: String): Boolean {
        val compact = line.trim().replace(" ", "")
        return compact.length >= 3 && compact.all { it == '-' } ||
            compact.length >= 3 && compact.all { it == '*' } ||
            compact.length >= 3 && compact.all { it == '_' }
    }

    private fun isTableSeparator(line: String): Boolean {
        val cells = tableCells(line)
        return cells.isNotEmpty() && cells.all { cell ->
            val value = cell.trim().removePrefix(":").removeSuffix(":")
            value.length >= 3 && value.all { it == '-' }
        }
    }

    private fun tableCells(line: String): List<String> =
        line.trim().removePrefix("|").removeSuffix("|").split('|').map(String::trim)
}

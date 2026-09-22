# Content and Markdown

## HTML-like typography

```kotlin
H1("Page title")
H2("Section")
H3("Subsection")
H4("Heading")
H5("Heading")
H6("Heading")
Lead("Article introduction")
Paragraph("Body copy")
Caption("Figure caption")
SmallText("Metadata")
```

這些 helper 使用 `MaterialTheme.typography`，可由 app theme 統一換字型與比例。

## Typed document

```kotlin
val guide = document {
    h1("Quick start")
    paragraph(text("Use "), strong("semantic"), text(" content."))
    bulletList("Desktop", "JavaScript", "Wasm")
    quote { paragraph("One source set, three targets.") }
    code("MarkdownResource(\"docs/guide.md\")", "kotlin")
}

DocumentRenderer(guide)
```

## Markdown string

```kotlin
MarkdownDocument("# Title\n\nA **strong** paragraph.")
```

## Markdown resource

把文件放在：

```text
shared/src/commonMain/composeResources/files/docs/guide.md
```

直接顯示：

```kotlin
MarkdownResource(
    path = "docs/guide.md",
    loading = { Spinner() },
    failure = { Alert(it.message ?: "Load failed", tone = Tone.Danger) },
)
```

## Supported syntax

- `#`–`######` headings
- Paragraphs and line breaks
- Strong and emphasis
- Inline and fenced code
- Clickable links
- Image nodes with text fallback
- Block quotes
- Ordered and unordered lists
- Horizontal rules
- Pipe tables

遠端圖片尚未綁定特定 image loader；請由產品選定 Coil、Kamel 或其他跨平台方案後接入。

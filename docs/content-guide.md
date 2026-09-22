# Semantic content and Markdown

The `designsystem.content` package provides HTML-like typography helpers, a strongly typed document model,
a Kotlin document DSL, Markdown parsing, and rendering from bundled resources.

## Semantic typography

```kotlin
H1("Account settings")
Lead("Manage the profile and security options for this account.")
H2("Profile")
Paragraph("Changes are saved immediately.")
Caption("Last updated today")
```

Available helpers are `H1` through `H6`, `Paragraph`, `Lead`, `Caption`, and `SmallText`. They map to
`MaterialTheme.typography`, so application typography remains centralized.

## Strongly typed documents

```kotlin
val quickStart = document {
    h1("Quick start")
    paragraph(text("Use "), strong("semantic"), text(" content."))
    h2("Install")
    orderedList("Add the dependency", "Run the application")
    code(
        """
        var formSubmitted by remember { mutableStateOf(false) }
        Button("Save", onClick = { formSubmitted = true })
        """.trimIndent(),
        language = "kotlin",
    )
}

DocumentRenderer(quickStart)
```

The document model is UI-independent. Content can therefore come from the Kotlin DSL, Markdown, a database,
or an API without changing the Compose renderer.

## Markdown strings

```kotlin
MarkdownDocument(
    markdown = """
        # Quick start

        This supports **strong text**, links, lists, quotes, code fences, and tables.
    """.trimIndent(),
)
```

Supported block syntax:

- ATX headings (`#` through `######`)
- paragraphs and line breaks
- fenced code blocks using backticks or tildes
- block quotes
- ordered and unordered lists
- horizontal rules
- pipe tables

Supported inline syntax includes strong text, emphasis, inline code, clickable links, image nodes, and escaped
characters. Images currently render an accessible text fallback; add a project image loader before using remote
Markdown images as visual content.

## Markdown from application resources

Put documents under `shared/src/commonMain/composeResources/files`, for example:

```text
shared/src/commonMain/composeResources/files/docs/getting-started.md
```

Render the resource directly:

```kotlin
MarkdownResource("docs/getting-started.md")
```

The helper uses Compose Multiplatform resources, so the same document works on JVM, JavaScript, and Wasm.
Pass `loading` and `failure` slots when the screen requires custom states:

```kotlin
MarkdownResource(
    path = "docs/getting-started.md",
    loading = { Text("Loading…") },
    failure = { error -> Alert(error.message ?: "Unable to load document") },
)
```

For non-UI usage, call `readTextResource("docs/getting-started.md")` from a coroutine.

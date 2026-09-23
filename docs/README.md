# ConsoleApp documentation

- [Component guide](./component-guide.md) — philosophy, quick start, usage patterns,
  API index, accessibility, and extension rules.
- [Complete component cookbook](./components.md) — every public component, state model,
  and copy-ready example.
- [Component API reference](./component-reference.md) — parameters, defaults, ownership,
  and appropriate use cases.
- [Material 3 components](./material3-components.md) — complete coverage table and
  copy-ready state, navigation, picker, search, sheet, and Snackbar examples.
- [Business recipes](./business-recipes.md) — end-to-end state, validation, repository,
  async result, and recovery flows.
- [Account settings tutorial](./tutorial-account-settings.md) — a complete reusable helper,
  persistence boundary, and tested business validation.
- [Compose Desktop file chooser](./desktop-file-chooser.md) — native system icons,
  file navigation, request options, and a complete form integration example.
- [Guided article authoring](./article-guide.md) — article types, structure,
  building blocks, examples, and writing checklist.
- [Semantic content and Markdown](./content-guide.md) — HTML-like typography,
  typed documents, parsing, and resources.
- [Component library reference](./component-library.md) — directory taxonomy,
  implementation coverage, Bootstrap mapping, and rollout status.

The same introduction is available inside the application through the
`ComponentGuide()` composable.

GitHub Wiki source files are maintained in [`../wiki`](../wiki/README.md).

## Example quality standard

- Copy-ready examples must define their state and callback dependencies.
- A business tutorial must show model, validation, loading, success, failure, persistence, and test.
- Names such as `::save`, `viewModel`, or `items` may not appear unexplained as if the framework supplied them.
- Short API snippets link to a complete, compiled example when setup would otherwise hide the business flow.

# ConsoleApp component library

> For day-to-day usage, start with the [component guide](./component-guide.md).
> This file is the architecture and coverage reference.

## Chosen approach

Use Compose Multiplatform components in `commonMain` and borrow Bootstrap's naming,
coverage, semantic colors, sizing, spacing, and responsive breakpoints. Do not import
Bootstrap CSS into shared Compose UI: the web app is rendered by Compose rather than
ordinary Bootstrap-styled HTML, and CSS would not cover the desktop target.

Platform capabilities stay behind adapters. A file field, for example, renders through
`FormFilePicker`, while JVM, JS, and Wasm code owns the actual native/browser picker.

## Folder taxonomy

```text
designsystem/
├── foundation/             # Shared enums, state contracts, accessibility contracts
├── tokens/                 # Color, spacing, radius, typography, elevation, breakpoints
├── components/
│   ├── actions/            # Button, button group, close button, links
│   ├── dataDisplay/        # Badge, table, list group, image, figure
│   ├── feedback/           # Alert, progress, spinner, skeleton, toast
│   ├── form/               # All form controls, validation, input groups, form layout
│   ├── layout/             # Container, responsive grid, row, column, stack, divider
│   ├── navigation/         # Navbar, nav, tabs, breadcrumb, pagination, scrollspy
│   ├── overlay/            # Modal, dropdown, popover, tooltip, offcanvas/drawer
│   └── surface/            # Card, accordion, collapse, carousel
├── patterns/               # Login form, settings form, filter bar, empty/error states
├── adapters/               # File/date/time pickers and other platform APIs
└── catalog/                # Gallery metadata and Bootstrap coverage table
```

Only folders with implemented code should be created. The tree above is the stable
destination structure, not a request to keep empty directories in Git.

## Form coverage

| Need | Public API | State |
| --- | --- | --- |
| Text, email, numeric, decimal, phone, URL, search | `FormTextInput` + `FormInputType` | Ready |
| Password visibility | `FormPasswordInput` | Ready |
| Textarea | `FormTextArea` | Ready |
| Single select | `FormSelect` | Ready |
| Multi select | `FormMultiSelect` | Ready |
| Checkbox | `FormCheckbox` | Ready |
| Radio group | `FormRadioGroup` | Ready |
| Switch | `FormSwitch` | Ready |
| Range | `FormRange` | Ready |
| Input prefix/suffix | `FormInputGroup` | Ready |
| Section, submit, reset | `FormSection`, `FormActions` | Ready |
| Required, email, length, regex, composed validation | `FormValidators` | Ready |
| File input presentation | `FormFilePicker` | Ready; actual picker needs adapters |
| Native date/time picker | `FormTextInput` supports values; native picker needs adapters | Adapter |

Every input supports a controlled value, disabled state where applicable, visible label,
helper/error content, and a `Modifier`. Validation is independent from UI so it can be
unit-tested and used from a ViewModel.

## Bootstrap rollout

1. **Foundation and forms** — tokens, validation, every basic form control, form gallery.
2. **Daily UI** — buttons, alerts, badges, cards, list groups, progress, spinner, skeleton.
3. **Navigation and overlays** — navbar, tabs, breadcrumb, pagination, modal, drawer,
   dropdown, tooltip, popover, toast. Completed.
4. **Advanced interaction** — accordion/collapse, carousel and controlled scrollspy are
   complete; responsive grid remains planned.

The machine-readable roadmap is `designsystem/catalog/BootstrapComponentCatalog.kt`.
All general components use one short import package and Bootstrap-compatible names:

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*
```

Public composables are `Accordion`, `Alert`, `Badge`, `Breadcrumb`, `Button`,
`ButtonGroup`, `Card`, `Carousel`, `CloseButton`, `Collapse`, `Dropdown`, `ListGroup`,
`Modal`, `Navbar`, `Nav`, `Tabs`, `Offcanvas`, `Pagination`, `Placeholder`, `Popover`,
`Progress`, `ScrollSpy`, `Spinner`, `Toast`, `ToastHost`, and `Tooltip`. The earlier `Ds*` APIs
remain available as a compatibility layer.

Supporting models are deliberately short as well: `AccordionItem`, `BreadcrumbItem`,
`ButtonItem`, `CarouselItem`, `DropdownItem`, `ListItem`, and `NavItem`.

### Quick usage

```kotlin
import cg.creamgod.consoleapp.designsystem.components.*

Button("儲存", onClick = ::save)
Alert("資料已更新", tone = Tone.Success)
Badge("New", pill = true)
Progress(0.65f)

Accordion(
    items = listOf(
        AccordionItem("基本資料", initiallyExpanded = true) {
            Text("Accordion content")
        },
        AccordionItem("進階設定") {
            Text("More content")
        },
    ),
)
```

Dialogs remain controlled, so screen or ViewModel state is always the source of truth:

```kotlin
var showDelete by remember { mutableStateOf(false) }

Confirm(
    visible = showDelete,
    title = "刪除資料？",
    message = "這個動作無法復原。",
    destructive = true,
    onConfirm = {
        delete()
        showDelete = false
    },
    onDismiss = { showDelete = false },
)
```

For a toast, mount one host in `Scaffold` and show messages from a coroutine:

```kotlin
val toast = rememberToastState()
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { ToastHost(toast) }) { padding ->
    Button("顯示 Toast", onClick = {
        scope.launch { toast.show("儲存完成") }
    })
}
```

If a screen also imports Material 3's `Button`, use an explicit alias:

```kotlin
import cg.creamgod.consoleapp.designsystem.components.Button as AppButton
```

## Feedback and overlay coverage

| Need | Public API | State |
| --- | --- | --- |
| Anchored/free popup | `Popup` | Ready |
| Queued toast and action | `ToastHost`, `ToastState` | Ready |
| Inline callout | `Alert`, `Callout` | Ready |
| Composable modal body/footer | `Modal` | Ready |
| Yes/no confirmation | `Confirm` | Ready |
| Text question | `Ask` | Ready |
| Single-choice question | `Choice` | Ready |

Put one `ToastHost` in the application's top-level `Scaffold`. Obtain its state with
`rememberToastState()` and call the suspending `show()` function from a coroutine.
Dialogs and popups are controlled components: `visible` is owned by the caller and every
dismiss path reports through `onDismissRequest`.

## Usage example

```kotlin
var email by remember { mutableStateOf("") }
val validation = FormValidators.all(
    FormValidators.required(),
    FormValidators.email(),
).validate(email)

FormTextInput(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    type = FormInputType.Email,
    required = true,
    errorText = validation.message,
    modifier = Modifier.fillMaxWidth(),
)
```

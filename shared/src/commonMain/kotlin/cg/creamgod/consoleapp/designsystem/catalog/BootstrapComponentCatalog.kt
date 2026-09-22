package cg.creamgod.consoleapp.designsystem.catalog

import cg.creamgod.consoleapp.designsystem.foundation.ComponentStatus

data class BootstrapComponentSpec(
    val category: String,
    val name: String,
    val composeEquivalent: String,
    val status: ComponentStatus,
)

/** Bootstrap 5.3 surface mapped to the planned Compose Multiplatform library. */
val bootstrapComponentCatalog = listOf(
    BootstrapComponentSpec("Layout", "Container / Grid / Columns / Gutters", "ResponsiveContainer, GridRow, GridColumn", ComponentStatus.Planned),
    BootstrapComponentSpec("Content", "Semantic typography / Documents / Markdown", "H1-H6, Paragraph, DocumentRenderer, MarkdownDocument, MarkdownResource", ComponentStatus.Ready),
    BootstrapComponentSpec("Content", "Typography / Images / Tables / Figures", "Typography, ResponsiveImage, DataTable, Figure", ComponentStatus.Planned),
    BootstrapComponentSpec("Forms", "Controls / Select / Checks / Radios / Range", "FormTextInput, FormSelect, FormCheckbox, FormRadioGroup, FormRange", ComponentStatus.Ready),
    BootstrapComponentSpec("Forms", "Input group / Layout / Validation", "FormInputGroup, FormSection, FormValidators", ComponentStatus.Ready),
    BootstrapComponentSpec("Forms", "File / Date / Time picker", "FormFilePicker plus platform picker adapters", ComponentStatus.PlatformAdapter),
    BootstrapComponentSpec("Components", "Accordion", "Accordion", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Alerts", "Alert, Callout", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Badge", "Badge", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Breadcrumb", "Breadcrumb", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Buttons", "Button", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Button group", "ButtonGroup", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Card", "Card", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Carousel", "Carousel", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Close button", "CloseButton", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Collapse", "Collapse", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Dropdowns", "Dropdown", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "List group", "ListGroup", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Modal", "Modal, Confirm, Ask, Choice", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Navbar", "Navbar", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Navs & tabs", "Nav, Tabs", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Offcanvas", "Offcanvas", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Pagination", "Pagination", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Placeholders", "Placeholder", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Popovers", "Popover", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Progress", "Progress", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Scrollspy", "ScrollSpy", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Spinners", "Spinner", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Toasts", "Toast, ToastHost, ToastState", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Tooltips", "Tooltip", ComponentStatus.Ready),
    BootstrapComponentSpec("Components", "Popup", "Popup", ComponentStatus.Ready),
    BootstrapComponentSpec("Helpers", "Stacks / Ratio / Truncation / Visually hidden", "Compose modifiers and layout helpers", ComponentStatus.Planned),
    BootstrapComponentSpec("Utilities", "Color / Display / Flex / Spacing / Sizing", "Tokens and modifier extensions", ComponentStatus.Planned),
)

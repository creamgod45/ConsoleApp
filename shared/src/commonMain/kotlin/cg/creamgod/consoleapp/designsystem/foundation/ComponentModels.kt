package cg.creamgod.consoleapp.designsystem.foundation

/** Sizes shared by controls that expose Bootstrap-like size variants. */
enum class ComponentSize {
    Small,
    Medium,
    Large,
}

/** Semantic intent. Components decide how each intent maps to Material colors. */
enum class ComponentTone {
    Primary,
    Secondary,
    Success,
    Danger,
    Warning,
    Info,
    Light,
    Dark,
}

enum class ComponentStatus {
    Ready,
    Planned,
    PlatformAdapter,
}

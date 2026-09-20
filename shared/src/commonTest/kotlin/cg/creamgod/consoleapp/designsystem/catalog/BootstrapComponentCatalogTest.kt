package cg.creamgod.consoleapp.designsystem.catalog

import cg.creamgod.consoleapp.designsystem.foundation.ComponentStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class BootstrapComponentCatalogTest {
    @Test
    fun everyBootstrapComponentHasAReadyComposeApi() {
        val expected = setOf(
            "Accordion",
            "Alerts",
            "Badge",
            "Breadcrumb",
            "Buttons",
            "Button group",
            "Card",
            "Carousel",
            "Close button",
            "Collapse",
            "Dropdowns",
            "List group",
            "Modal",
            "Navbar",
            "Navs & tabs",
            "Offcanvas",
            "Pagination",
            "Placeholders",
            "Popovers",
            "Progress",
            "Scrollspy",
            "Spinners",
            "Toasts",
            "Tooltips",
        )

        val ready = bootstrapComponentCatalog
            .filter { it.category == "Components" && it.status == ComponentStatus.Ready }
            .map { it.name }
            .toSet()

        assertEquals(expected, ready.intersect(expected))
    }
}

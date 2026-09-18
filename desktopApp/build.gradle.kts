import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "cg.creamgod.consoleapp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cg.creamgod.consoleapp"
            packageVersion = "1.0.0"
        }
    }
}
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// CI 以 -PappVersion=1.2.3 覆寫；本機開發沿用預設值。
// jpackage 對 msi/dmg 只接受 x.y.z 格式，所以這裡不接受任何後綴。
val appVersion: String = providers.gradleProperty("appVersion").getOrElse("1.0.0")

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.material3.desktop)
    implementation(libs.okhttp)
    testImplementation(libs.kotlin.testJunit)
}

compose.desktop {
    application {
        mainClass = "cg.creamgod.consoleapp.MainKt"

        nativeDistributions {
            // 每種格式只能在對應的 OS 上打包（jpackage 無法交叉編譯），
            // 不相容的格式會在當前平台自動略過。
            targetFormats(
                TargetFormat.Deb,  // Linux: Debian / Ubuntu
                TargetFormat.Rpm,  // Linux: Fedora / RHEL / openSUSE
                TargetFormat.Msi,  // Windows
                TargetFormat.Dmg,  // macOS
            )
            packageName = "cg.creamgod.consoleapp"
            packageVersion = appVersion
            description = "ConsoleApp - Compose Multiplatform desktop application"
            vendor = "creamgod45"

            linux {
                shortcut = true
                menuGroup = "ConsoleApp"
                appCategory = "Utility"
                // jpackage 會把這個寫進 .deb 的 control 檔
                debMaintainer = "ConsoleAppMaintance@icloud.com"
                // repo 目前沒有 LICENSE 檔，先留 Unknown；加上授權後改成對應的 SPDX 代碼
                rpmLicenseType = "Unknown"
            }

            windows {
                menu = true
                shortcut = true
                perUserInstall = true
                // 固定不變，讓後續版本能就地升級而不是並存安裝。改掉會讓舊版無法被覆蓋。
                upgradeUuid = "48f87d27-970c-4d74-801b-384d82c5b5f4"
            }

            macOS {
                bundleID = "cg.creamgod.consoleapp"
                dockName = "ConsoleApp"
                appCategory = "public.app-category.utilities"
            }
        }
    }
}

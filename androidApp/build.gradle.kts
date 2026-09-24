plugins {
    // AGP 9 內建 Kotlin 支援，不需要再套用 org.jetbrains.kotlin.android
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// CI 以 -PappVersion=1.2.3 覆寫；本機開發沿用預設值。
val appVersion: String = providers.gradleProperty("appVersion").getOrElse("1.0.0")

// Play / adb 升級靠 versionCode，必須單調遞增，所以從 x.y.z 推導：
// major * 10000 + minor * 100 + patch（每段上限 99）。需要自訂時用 -PappVersionCode=123 覆寫。
fun versionCodeOf(version: String): Int {
    val parts = version.split(".").map { it.substringBefore("-").toIntOrNull() ?: 0 }
    return parts.getOrElse(0) { 0 } * 10000 + parts.getOrElse(1) { 0 } * 100 + parts.getOrElse(2) { 0 }
}

val appVersionCode: Int = providers.gradleProperty("appVersionCode")
    .map(String::toInt)
    .getOrElse(versionCodeOf(appVersion))

// Emulator 可透過 10.0.2.2 連到開發電腦；實機可用 -PapiBaseUrl=http://<電腦區網 IP>:8000 覆寫。
val apiBaseUrl = providers.gradleProperty("apiBaseUrl").getOrElse("http://172.18.180.75:8000")
val escapedApiBaseUrl = apiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")

// 簽章資料從 gradle property 或環境變數來，兩者都沒有時 release 會產出未簽署的 APK
// （檔名會是 *-unsigned.apk，無法直接安裝）。不要把 keystore 或密碼提交進 repo。


val keystorePath = providers.gradleProperty("androidKeystorePath")
    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PATH"))
val keystorePassword = providers.gradleProperty("androidKeystorePassword")
    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD"))
val keystoreKeyAlias = providers.gradleProperty("androidKeyAlias")
    .orElse(providers.environmentVariable("ANDROID_KEY_ALIAS"))
val keystoreKeyPassword = providers.gradleProperty("androidKeyPassword")
    .orElse(providers.environmentVariable("ANDROID_KEY_PASSWORD"))

android {
    namespace = "cg.creamgod.consoleapp"
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt()) {
            minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
        }
    }

    defaultConfig {
        applicationId = "cg.creamgod.consoleapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion
        buildConfigField("String", "API_BASE_URL", "\"$escapedApiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (keystorePath.isPresent) {
            create("release") {
                storeFile = file(keystorePath.get())
                storePassword = keystorePassword.get()
                keyAlias = keystoreKeyAlias.get()
                keyPassword = keystoreKeyPassword.orElse(keystorePassword).get()
            }
        }
    }

    buildTypes {
        getByName("release") {
            // Compose 的 runtime 反射面很廣，先不開 R8，避免發版時才發現被裁掉的類別。
            isMinifyEnabled = false
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.okhttp)

    debugImplementation(libs.compose.uiToolingPreview)
}

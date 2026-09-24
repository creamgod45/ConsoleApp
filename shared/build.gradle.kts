import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    // AGP 9 起 KMP 模組要用 com.android.kotlin.multiplatform.library，
    // Android 設定寫在 kotlin.androidLibrary 裡，不再有獨立的 android {} 區塊。
    android {
        namespace = "cg.creamgod.consoleapp.shared"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        // commonMain/composeResources 要進 APK 就得開這個
        androidResources {
            enable = true
        }

        // 讓 commonTest 也能在 Android 的 host test（JVM）上跑
        withHostTestBuilder {
        }
    }

    js {
        browser {
            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        // Android 的 host test 跑在 JVM 上，kotlin-test 需要 JUnit4 的實作
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.testJunit)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

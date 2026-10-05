// Top-level build file. On AGP 9 Kotlin is built into the Android plugin, so there is no
// separate kotlin-android plugin; Compose uses the Kotlin Compose compiler plugin.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}

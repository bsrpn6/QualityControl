plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("com.google.gms.google-services") version "4.3.10" apply false
    id("com.google.firebase.crashlytics") version "2.9.7" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("com.google.devtools.ksp") version "1.8.20-1.0.11" apply false
}
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("com.google.gms:google-services:4.4.1")
    }
}

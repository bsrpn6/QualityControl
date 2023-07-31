plugins {
//    id("com.android.application") version "7.0.2" apply false
//    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
//    id("com.google.devtools.ksp") version "1.5.31-1.0.0" apply false
    id ("com.android.application") version "8.0.0" apply false
    id ("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id ("com.android.library") version "8.0.0" apply false
}
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

// Top-level build.gradle file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Make sure that you have the AGP plugin 8.1+ dependency
    id("com.android.application") version "8.1.4" apply false
    // ...

    // Make sure that you have the Google services Gradle plugin 4.4.1+ dependency
    id("com.google.gms.google-services") version "4.4.2" apply false

    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Use only one version of google-services
        classpath ("com.google.gms:google-services:4.3.15") // Keep the latest version

        // Use the correct version of the Android Gradle plugin, avoid duplicates
        classpath ("com.android.tools.build:gradle:8.5.1")
    }
}

//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//        maven { setUrl("https://jitpack.io") }    }
//}





//plugins {
//    alias(libs.plugins.android.application) apply false
//    id("com.google.gms.google-services") version "4.4.2" apply false}
//buildscript {
//    dependencies {
//        classpath ("com.google.gms:google-services:4.3.14")
//        classpath ("com.google.gms:google-services:4.3.15")
//        classpath ("com.android.tools.build:gradle:8.0.0")
//
//    }
//}

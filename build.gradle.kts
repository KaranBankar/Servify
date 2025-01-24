// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("com.google.devtools.ksp") // You already have this here
}

buildscript {
    repositories {
        google() // Ensure this is included
        mavenCentral() // Ensure this is included

    }
    dependencies {
        // Add the KSP plugin classpath here
           }
}

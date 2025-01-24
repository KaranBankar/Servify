plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.servify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.servify"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.core:core:1.15.0")
    implementation("androidx.core:core-ktx:1.15.0")

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.google.firebase:firebase-database:20.3.0")

// Firebase Realtime Database
    implementation ("com.google.firebase:firebase-database-ktx")

// Firebase Authentication
    implementation ("com.google.firebase:firebase-auth-ktx")

// Optional: Firebase Analytics (if you plan to use it)
    implementation ("com.google.firebase:firebase-analytics-ktx")

// Optional: Firebase Cloud Messaging (if you plan to use push notifications)
    implementation ("com.google.firebase:firebase-messaging-ktx")

}
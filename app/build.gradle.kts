plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.calodiary"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.calodiary"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true  // Enable View Binding for easier UI handling
    }
}

dependencies {
    // Firebase BOM to manage version consistency
    implementation(platform(libs.firebase.bom))  // Use BOM to avoid version mismatches

    // Firebase dependencies
    implementation(libs.google.firebase.auth)    // Firebase Authentication
    implementation(libs.firebase.firestore)      // Firestore for username-email mapping

    // Core AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.11.0")
    // glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    //FireBase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
}
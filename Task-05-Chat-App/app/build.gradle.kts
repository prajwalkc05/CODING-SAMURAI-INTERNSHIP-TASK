plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.firebasechatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.firebasechatapp"
        minSdk = 23
        targetSdk = 34
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

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase (BoM controls versions)
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging") // Added FCM

    // Image loading (chat images)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // SMS Retriever (OTP auto-read) - Keeping it as it was in original file, though maybe unused now
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.2")

    // Country Code Picker - Keeping it
    implementation("com.hbb20:ccp:2.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
}

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}

android {
    namespace = "com.wharzc.motoguard"
    compileSdk = 36 // Changed to stable 35 to avoid preview SDK bugs, but you can change back to 36 if needed

    defaultConfig {
        applicationId = "com.wharzc.motoguard"
        minSdk = 33
        targetSdk = 36
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

    // ADDED: This is the missing magic block that enables Jetpack Compose!
    buildFeatures {
        compose = true
        buildConfig = true //
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // 1. CORE ANDROID
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation)

    // 2. COMPOSE BILL OF MATERIALS (BOM)
    val composeBom = platform("androidx.compose:compose-bom:2024.02.02")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("org.maplibre.gl:android-sdk:11.11.0")
    implementation("androidx.core:core-ktx:1.12.0")


    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.google.firebase:firebase-firestore-ktx")

    implementation ("com.google.firebase:firebase-database-ktx")


    // 3. MATERIAL 3 & UI COMPONENTS
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // 4. OUR MOTOGUARD ADDITIONS (Cleaned up duplicates)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")

    // Google Maps & Location
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.maps.android:android-maps-utils:3.8.0")
    implementation("com.google.android.gms:play-services-tasks:18.0.1")

    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("org.maplibre.gl:android-sdk:11.11.0")
    implementation("org.maplibre.compose:maplibre-compose-material3:0.11.1")

    // IoT Networking & Data (Retrofit & Firebase)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    //Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")






    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
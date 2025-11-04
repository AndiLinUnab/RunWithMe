plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "me.andilin.runwithme"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.andilin.runwithme"
        minSdk = 26
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom.v3351))

    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.common.ktx)

    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.lifecycle.runtime.ktx.v284)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom.v20251001))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.compose.v280)
    implementation(libs.coil.compose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(platform(libs.compose.bom.v20240902))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

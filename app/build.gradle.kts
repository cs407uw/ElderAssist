plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "1.9.0"
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("io.github.takahirom.roborazzi") version "1.8.0-alpha-5"
}

android {
    compileSdk = 35
    namespace = "com.cs407.elderassist_tutorial"

    defaultConfig {
        applicationId = "com.cs407.elderassist_tutorial"
        minSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

// Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // AndroidX libraries
    implementation(libs.androidx.appcompat)
    implementation (libs.androidx.paging.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.fragment.testing)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // ML Kit libraries
    implementation(libs.barcode.scanning)
    implementation(libs.text.recognition)
    implementation (libs.face.detection)
    implementation (libs.image.labeling)

    // Google Play Services
    implementation(libs.gms.play.services.location)
    implementation(libs.play.services.maps)

    // Networking libraries
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.glide)

    // Other libraries
    implementation (libs.opencsv)
    implementation (libs.gson)

    implementation(libs.androidx.room.ktx)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.espresso.contrib)
    testImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.androidx.rules)
    testImplementation(libs.androidx.runner)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.appcompat)
    implementation (libs.gms.play.services.location)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)


}

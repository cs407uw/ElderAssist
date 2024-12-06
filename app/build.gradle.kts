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

//dependencies {
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("com.google.mlkit:barcode-scanning:17.3.0")
//    implementation("com.google.mlkit:text-recognition:16.0.1")
//    implementation("com.google.android.gms:play-services-location:21.3.0")
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.squareup.moshi:moshi:1.15.1")
//    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
//    implementation("com.github.bumptech.glide:glide:4.16.0")
//    implementation("com.google.android.material:material:1.12.0")
//
//    implementation ("androidx.paging:paging-runtime:3.1.1")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//
//    implementation(libs.androidx.room.paging)
//    implementation(libs.androidx.room.runtime)
//    implementation(libs.androidx.navigation.fragment)
//    implementation(libs.androidx.paging.runtime.ktx)
//    implementation(libs.androidx.junit.ktx)
//    implementation(libs.androidx.fragment.testing)
//    annotationProcessor(libs.androidx.room.compiler)
//    // To use Kotlin Symbol Processing (KSP)
//    ksp(libs.androidx.room.compiler)
//    implementation ("com.opencsv:opencsv:5.7.1")
//    implementation ("com.google.code.gson:gson:2.8.9")
//    implementation(libs.androidx.navigation.fragment.ktx)
//    implementation(libs.androidx.navigation.ui.ktx)
//
//    implementation("com.google.android.gms:play-services-maps:18.0.2")
//    implementation("com.google.android.gms:play-services-location:21.0.1")
//
//
//    implementation(libs.androidx.room.ktx)
//
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    testImplementation(libs.androidx.espresso.core)
//
//    testImplementation("org.robolectric:robolectric:4.13")
//
//    // Espresso dependencies
//    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
//    testImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
//    testImplementation("androidx.test.espresso:espresso-intents:3.6.1")
//    testImplementation("androidx.test:rules:1.6.1")
//    testImplementation("androidx.test:runner:1.6.2")
//
//    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.8.0-alpha-5")
//    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.8.0-alpha-5")
//
//    testImplementation("org.mockito:mockito-core:5.12.0")
//    testImplementation("org.mockito:mockito-inline:3.11.2")
//
//    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
//    testImplementation(libs.core.testing)
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
//
//    implementation ("com.google.mlkit:face-detection:16.1.5")
//
//    // text recognition
//    implementation ("com.google.mlkit:text-recognition:16.0.0")
//
//    // image labeling
//    implementation ("com.google.mlkit:image-labeling:17.0.7")
//
//}
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.exifinterface:exifinterface:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.15.0")

// Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // AndroidX libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.paging:paging-runtime:3.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
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
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation ("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.mlkit:image-labeling:17.0.7")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:18.0.2")

    // Networking libraries
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Other libraries
    implementation ("com.opencsv:opencsv:5.7.1")
    implementation ("com.google.code.gson:gson:2.8.9")

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.espresso.core)
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    testImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    testImplementation("androidx.test:rules:1.6.1")
    testImplementation("androidx.test:runner:1.6.2")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.8.0-alpha-5")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.8.0-alpha-5")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-inline:3.11.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation(libs.core.testing)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")

}
